// Convex OIDC provider configuration for Microsoft Entra ID.
// Replace placeholders before production use.
export default {
  providers: [
    {
      domain: "https://login.microsoftonline.com/<ENTRA_TENANT_ID>/v2.0",
      applicationID: "<ENTRA_CLIENT_ID>",
    },
  ],
};
