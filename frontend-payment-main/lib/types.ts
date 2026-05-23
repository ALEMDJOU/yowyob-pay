export type Wallet = {
  id: string;
  ownerId: string;
  ownerName: string;
  balance: number;
};

export type PagedWallets = {
  content: Wallet[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Transaction = {
  id: string;
  walletId: string;
  amount: number;
  type: "RECHARGE" | "PAYMENT";
  status: string;
};

export type Session = {
  agentId: string;
  email: string;
  name: string;
};
