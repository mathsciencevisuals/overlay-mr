export function getEnv() {
  return {
    port: Number(process.env.PORT || 8080),
    gcpProjectId: process.env.GCP_PROJECT_ID || '',
    assetBucket: process.env.ASSET_BUCKET || '',
    signedUrlTtlSeconds: Number(process.env.SIGNED_URL_TTL_SECONDS || 900),
    firestoreEnabled: process.env.FIRESTORE_ENABLED === 'true',
  };
}
