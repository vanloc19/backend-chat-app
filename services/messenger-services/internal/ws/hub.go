package ws

type Hub struct {
	// TODO: bổ sung map client, room, broadcast channel...
	register   chan *Client
	unregister chan *Client
}

func NewHub() *Hub {
	return &Hub{
		register:   make(chan *Client),
		unregister: make(chan *Client),
	}
}

func (h *Hub) Run() {
	for {
		select {
		case client := <-h.register:
			_ = client
			// TODO: lưu client vào map, xử lý join room
		case client := <-h.unregister:
			_ = client
			// TODO: xóa client khỏi map, cleanup
		}
	}
}

