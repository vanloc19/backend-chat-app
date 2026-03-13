package middleware

import "net/http"

func CORS(frontendOrigin string, next http.Handler) http.Handler {
	allowedOrigins := map[string]struct{}{
		"http://127.0.0.1:5173": {},
		frontendOrigin:        {},
	}

	if frontendOrigin == "" {
		delete(allowedOrigins, "")
	}

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		origin := r.Header.Get("Origin")

		if origin != "" {
			if _, ok := allowedOrigins[origin]; ok {
				w.Header().Set("Access-Control-Allow-Origin", origin)
				w.Header().Set("Vary", "Origin")
				w.Header().Set("Access-Control-Allow-Credentials", "true")
				w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")
				w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
			}
		}

		if r.Method == http.MethodOptions {
			if origin != "" {
				if _, ok := allowedOrigins[origin]; !ok {
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