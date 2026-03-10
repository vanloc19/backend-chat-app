package app

import (
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"

	"api-gateway/internal/config"
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
		// giữ nguyên path sau prefix /auth, /friends,...
	}

	if enableWebSocket {
		proxy.Transport = &http.Transport{
			Proxy: http.ProxyFromEnvironment,
		}
	}

	return proxy
}

func New() *App {
	cfg := config.Load()
	mux := http.NewServeMux()

	base := func(urlStr, port string) string {
		return fmt.Sprintf("%s:%s", urlStr, port)
	}

	authProxy := newReverseProxy(base(cfg.AuthServiceURL, cfg.AuthPort), false)
	friendProxy := newReverseProxy(base(cfg.FriendServiceURL, cfg.FriendPort), false)
	messagerProxy := newReverseProxy(base(cfg.MessagerServiceURL, cfg.MessagerPort), true)
	notificationProxy := newReverseProxy(base(cfg.NotificationURL, cfg.NotificationPort), false)

	mux.Handle("/auth/", http.StripPrefix("/auth", authProxy))
	mux.Handle("/friends/", http.StripPrefix("/friends", friendProxy))
	mux.Handle("/messager/", http.StripPrefix("/messager", messagerProxy))
	mux.Handle("/notifications/", http.StripPrefix("/notifications", notificationProxy))

	server := &http.Server{
		Addr:    fmt.Sprintf(":%s", cfg.GatewayPort),
		Handler: mux,
	}

	return &App{server: server}
}

func (a *App) Run() error {
	log.Println("Go API Gateway listening on", a.server.Addr)
	return a.server.ListenAndServe()
}

