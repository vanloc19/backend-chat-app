package main

import (
	"log"

	"api-gateway/internal/app"
)

func main() {
	a := app.New()
	if err := a.Run(); err != nil {
		log.Fatal(err)
	}
}

