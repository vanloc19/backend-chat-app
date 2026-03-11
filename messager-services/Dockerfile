# Build stage
FROM golang:1.24-alpine AS build
WORKDIR /app
COPY go.mod go.sum ./
COPY . .
RUN go mod tidy && go build -o server ./cmd/server

# Run stage
FROM alpine:3.18
WORKDIR /app
COPY --from=build /app/server .
EXPOSE 5003
CMD ["./server"]
