export interface JwtPayload {
  sub: string       // email
  roles: string[]
  iat: number
  exp: number
}

export function decodeJwt(token: string): JwtPayload {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return { sub: '', roles: [], iat: 0, exp: 0 }
  }
}

export function isTokenExpired(token: string): boolean {
  const { exp } = decodeJwt(token)
  return Date.now() / 1000 > exp
}
