import type { NextRequest } from "next/server";
import {
  requireSessionAgentMatch,
  proxyUserPayment,
} from "@/lib/bff/userPaymentProxy";

type Params = { params: Promise<{ agentId: string }> };

export async function POST(request: NextRequest, { params }: Params) {
  const { agentId } = await params;
  const gate = await requireSessionAgentMatch(request, agentId);
  if (gate.error) {
    return gate.error;
  }
  const body = await request.text();
  return proxyUserPayment("POST", `/api/v1/agents/${agentId}/recharge`, {
    body,
    bearerFromCookie: true,
  });
}
