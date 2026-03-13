package app

import (
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"strings"

	"api-gateway/internal/config"
	"api-gateway/internal/middleware"

	"github.com/golang-jwt/jwt/v5"
)

type App struct {
	server *http.Server
}

func newReverseProxy(target string, enableWebSocket bool) *httputil.ReverseProxy {
	targetURL, err := url.Parse(target)
	if err != nil {
		log.Fatalf("invalid proxy target %s: %v", target, err)
	}

	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	originalDirector := proxy.Director
	proxy.Director = func(req *http.Request) {
		originalDirector(req)
	}

	if enableWebSocket {
		proxy.Transport = &http.Transport{
			Proxy: http.ProxyFromEnvironment,
		}
	}

	return proxy
}

// publicRoutes are paths that skip JWT verification.
var publicRoutes = []string{
	"/auth/api/auth/login",
	"/auth/api/auth/register",
	"/auth/api/auth/send-otp",
	"/auth/api/auth/verify-otp",
	"/auth/api/auth/refresh",
	"/auth/api/auth/logout",
	"/health",
}

func isPublicRoute(path string) bool {
	for _, route := range publicRoutes {
		if strings.HasPrefix(path, route) {
			return true
		}
	}
	return false
}

func jwtMiddleware(secret string, next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if isPublicRoute(r.URL.Path) {
			next.ServeHTTP(w, r)
			return
		}

		tokenStr := extractAccessToken(r)
		if tokenStr == "" {
			http.Error(w, "unauthorized: missing token", http.StatusUnauthorized)
			return
		}

		token, err := jwt.Parse(tokenStr, func(t *jwt.Token) (interface{}, error) {
			if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("unexpected signing method: %v", t.Header["alg"])
			}
			return []byte(secret), nil
		})

		if err != nil || !token.Valid {
			http.Error(w, "unauthorized: invalid token", http.StatusUnauthorized)
			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			http.Error(w, "unauthorized: invalid token claims", http.StatusUnauthorized)
			return
		}

		userID, ok := claims["userId"].(string)
		if !ok || strings.TrimSpace(userID) == "" {
			http.Error(w, "unauthorized: missing user id", http.StatusUnauthorized)
			return
		}

		r.Header.Set("X-User-Id", strings.TrimSpace(userID))
		if subject, err := claims.GetSubject(); err == nil && strings.TrimSpace(subject) != "" {
			r.Header.Set("X-User-Phone", strings.TrimSpace(subject))
		}

		next.ServeHTTP(w, r)
	})
}

func extractAccessToken(r *http.Request) string {
	authHeader := r.Header.Get("Authorization")
	if strings.HasPrefix(authHeader, "Bearer ") {
		return strings.TrimPrefix(authHeader, "Bearer ")
	}

	if cookie, err := r.Cookie("accessToken"); err == nil && cookie.Value != "" {
		return cookie.Value
	}

	return ""
}

func New() *App {
	cfg := config.Load()
	mux := http.NewServeMux()

	// Health check
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("ok"))
	})

	// Proxies
	authProxy         := newReverseProxy(cfg.AuthServiceURL, false)
	usersProxy        := newReverseProxy(cfg.UsersServiceURL, false)
	deviceProxy       := newReverseProxy(cfg.DeviceServiceURL, false)
	friendProxy       := newReverseProxy(cfg.FriendServiceURL, false)
	messengerProxy    := newReverseProxy(cfg.MessengerServiceURL, true)
	notificationProxy := newReverseProxy(cfg.NotificationURL, false)

	// Routes — prefix stripped khi forward xuống service
	mux.Handle("/auth/", http.StripPrefix("/auth", authProxy))
	mux.Handle("/users/", http.StripPrefix("/users", usersProxy))
	mux.Handle("/devices/", http.StripPrefix("/devices", deviceProxy))
	mux.Handle("/friends/", http.StripPrefix("/friends", friendProxy))
	mux.Handle("/messenger/", http.StripPrefix("/messenger", messengerProxy))
	mux.Handle("/notifications/", http.StripPrefix("/notifications", notificationProxy))

	server := &http.Server{
		Addr:    fmt.Sprintf(":%s", cfg.GatewayPort),
		Handler: middleware.CORS(cfg.FrontendOrigin, jwtMiddleware(cfg.JWTSecret, mux)),
	}

	return &App{server: server}
}

func (a *App) Run() error {
	log.Println("API Gateway listening on", a.server.Addr)
	return a.server.ListenAndServe()
}
