# Build stage
FROM golang:1.24-alpine AS build
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go build -o gateway ./cmd/server

# Run stage
FROM alpine:3.20
WORKDIR /app
COPY --from=build /app/gateway .
EXPOSE 5000
CMD ["./gateway"]
