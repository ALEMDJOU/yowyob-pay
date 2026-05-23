export type WalletDto = {
  id: string;
  ownerId: string;
  ownerName: string;
  balance: number;
};

export type PagedWalletsDto = {
  content: WalletDto[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type TransactionDto = {
  id: string;
  walletId: string;
  amount: number;
  type: string;
  status: string;
};

export type SessionDto = {
  agentId: string;
  email: string;
  name: string;
};

export type ProblemDetail = {
  title?: string;
  detail?: string;
  status?: number;
};
