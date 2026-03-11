package config

import (
	"context"
	"database/sql"
	"log"
	"os"
	"strings"

	_ "github.com/jackc/pgx/v5/stdlib"
)

func NewPostgresDB() *sql.DB {
	dsn := strings.TrimSpace(os.Getenv("DB_MESSAGER_URL"))
	if dsn == "" {
		log.Println("[DB] DB_MESSAGER_URL not set, skipping DB connection")
		return nil
	}

	db, err := sql.Open("pgx", dsn)
	if err != nil {
		log.Printf("[DB] Failed to open PostgreSQL: %v", err)
		return nil
	}

	if err := db.PingContext(context.Background()); err != nil {
		log.Printf("[DB] Failed to ping PostgreSQL: %v", err)
		return nil
	}

	log.Println("[DB] Connected to PostgreSQL (messager-services)")
	return db
}
