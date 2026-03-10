package app

import (
	"log"
	"net/http"

	"messager-services/internal/ws"
)

type App struct {
	server *http.Server
	hub    *ws.Hub
}

func New() *App {
	hub := ws.NewHub()

	mux := http.NewServeMux()
	mux.HandleFunc("/ws", ws.NewHandler(hub))

	server := &http.Server{
		Addr:    ":5003",
		Handler: mux,
	}

	return &App{
		server: server,
		hub:    hub,
	}
}

func (a *App) Run() error {
	go a.hub.Run()

	log.Println("WebSocket server listening on :5003")
	return a.server.ListenAndServe()
}

