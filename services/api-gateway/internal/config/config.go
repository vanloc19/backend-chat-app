package config

import "os"

type Config struct {
	GatewayPort        string
	JWTSecret          string
	AuthServiceURL     string
	UsersServiceURL    string
	DeviceServiceURL   string
	FriendServiceURL   string
	MessengerServiceURL string
	NotificationURL    string
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
		JWTSecret:           mustEnv("JWT_SECRET"),
		AuthServiceURL:      mustEnv("AUTH_SERVICE_URL"),
		UsersServiceURL:     mustEnv("USERS_SERVICE_URL"),
		DeviceServiceURL:    mustEnv("DEVICE_SERVICE_URL"),
		FriendServiceURL:    mustEnv("FRIEND_SERVICE_URL"),
		MessengerServiceURL: mustEnv("MESSENGER_SERVICE_URL"),
		NotificationURL:     mustEnv("NOTIFICATION_SERVICE_URL"),
	}
}
