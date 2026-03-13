export type NextFunction = () => void;

export type RequestLike = {
  method?: string;
  originalUrl?: string;
  url?: string;
  ip?: string;
};

export type ResponseLike = {
  statusCode?: number;
  on(event: "finish", listener: () => void): void;
};
