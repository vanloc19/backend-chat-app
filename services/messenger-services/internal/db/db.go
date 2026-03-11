package db

import (
	"database/sql"
	"log"

	_ "github.com/jackc/pgx/v5/stdlib"
)

func Connect(dsn string) *sql.DB {
	db, err := sql.Open("pgx", dsn)
	if err != nil {
		log.Fatalf("failed to open Postgres connection: %v", err)
	}

	if err := db.Ping(); err != nil {
		log.Fatalf("failed to ping Postgres: %v", err)
	}

	log.Println("Connected to Postgres for messager-services")
	return db
}

