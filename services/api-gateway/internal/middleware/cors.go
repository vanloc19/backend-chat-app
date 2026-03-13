package middleware

import (
	"net/http"
)

func CORS(frontendOrigin string, next http.Handler) http.Handler {
	allowedOrigins := []string{"null"}
	if frontendOrigin != "" {
		allowedOrigins = append(allowedOrigins, frontendOrigin)
	}

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		origin := r.Header.Get("Origin")
		originAllowed := false
		for _, allowedOrigin := range allowedOrigins {
			if origin == allowedOrigin {
				originAllowed = true
				break
			}
		}

		if origin != "" {
			if originAllowed {
				w.Header().Set("Access-Control-Allow-Origin", origin)
				w.Header().Set("Vary", "Origin")
				w.Header().Set("Access-Control-Allow-Credentials", "true")
				w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
				w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
			}
		}

		if r.Method == http.MethodOptions {
			if origin != "" {
				if !originAllowed {
					http.Error(w, "cors origin not allowed", http.StatusForbidden)
					return
				}
			}

			w.WriteHeader(http.StatusNoContent)
			return
		}

		next.ServeHTTP(w, r)
	})
}