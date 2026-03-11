package config

import (
	"log"
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	DBURL string
	Port  string
}

func mustEnv(key string) string {
	v := os.Getenv(key)
	if v == "" {
		panic("missing required env: " + key)
	}
	return v
}

func Load() *Config {
	// Local dev: tự load file .env (Docker/production sẽ dùng env thật)
	if err := godotenv.Load(); err != nil {
		// không crash nếu thiếu .env (ví dụ chạy trong container)
		log.Printf("warn: cannot load .env: %v", err)
	}
	return &Config{
		DBURL: mustEnv("DB_MESSAGER_URL"),
		Port:  mustEnv("MESSAGER_PORT"),
	}
}

