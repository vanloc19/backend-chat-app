package main

import (
	"log"

	"messager-services/internal/app"
)

func main() {
	a := app.New()

	if err := a.Run(); err != nil {
		log.Fatal(err)
	}
}

