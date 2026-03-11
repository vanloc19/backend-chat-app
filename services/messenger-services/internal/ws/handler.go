package ws

import (
	"log"
	"net/http"

	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		// TODO: cấu hình kiểm tra origin cho phù hợp môi trường deploy
		return true
	},
}

func NewHandler(hub *Hub) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		conn, err := upgrader.Upgrade(w, r, nil)
		if err != nil {
			log.Println("upgrade error:", err)
			return
		}

		client := &Client{
			conn: conn,
		}

		hub.register <- client

		// TODO: xử lý đọc/ghi message, close connection, unregister client...
	}
}

