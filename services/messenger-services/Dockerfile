# Build stage
FROM golang:1.24-alpine AS build
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go build -o server ./cmd/server

# Run stage
FROM alpine:3.20
WORKDIR /app
COPY --from=build /app/server .
EXPOSE 5003
CMD ["./server"]
