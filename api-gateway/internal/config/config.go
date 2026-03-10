package config

import "os"

type Config struct {
	GatewayPort         string
	AuthServiceURL      string
	AuthPort            string
	FriendServiceURL    string
	FriendPort          string
	MessagerServiceURL  string
	MessagerPort        string
	NotificationURL     string
	NotificationPort    string
}

func mustEnv(key string) string {
	v := os.Getenv(key)
	if v == "" {
		panic("missing required env: " + key)
	}
	return v
}

func Load() *Config {
	return &Config{
		GatewayPort:         mustEnv("GATEWAY_PORT"),
		AuthServiceURL:      mustEnv("AUTH_SERVICE_URL"),
		AuthPort:            mustEnv("AUTH_PORT"),
		FriendServiceURL:    mustEnv("FRIEND_SERVICE_URL"),
		FriendPort:          mustEnv("FRIEND_PORT"),
		MessagerServiceURL:  mustEnv("MESSAGER_SERVICE_URL"),
		MessagerPort:        mustEnv("MESSAGER_PORT"),
		NotificationURL:     mustEnv("NOTIFICATION_SERVICE_URL"),
		NotificationPort:    mustEnv("NOTIFICATION_PORT"),
	}
}

