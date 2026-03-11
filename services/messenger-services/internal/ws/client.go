package ws

import "github.com/gorilla/websocket"

type Client struct {
	conn *websocket.Conn
	// TODO: bổ sung id user, room, channel gửi/nhận...
}

