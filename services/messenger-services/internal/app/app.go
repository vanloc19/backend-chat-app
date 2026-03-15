package app

import (
	"log"
	"net/http"

	"messager-services/internal/config"
	"messager-services/internal/db"
	"messager-services/internal/ws"
)

type App struct {
	server *http.Server
	hub    *ws.Hub
	db     interface{}
}

func New() *App {
	cfg := config.Load()
	hub := ws.NewHub()

	pg := db.Connect(cfg.DBURL)

	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})
	mux.HandleFunc("/ws", ws.NewHandler(hub))

	server := &http.Server{
		Addr:    ":" + cfg.Port,
		Handler: mux,
	}

	return &App{
		server: server,
		hub:    hub,
		db:     pg,
	}
}

func (a *App) Run() error {
	go a.hub.Run()
	log.Printf("WebSocket server listening on %s", a.server.Addr)
	return a.server.ListenAndServe()
}
