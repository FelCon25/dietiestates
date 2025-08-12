export const getPasswordResetTemplate = (url: string) => ({
  subject: "Password Reset Request",
  text: `You requested a password reset. Click on the link to reset your password: ${url}`,
  html: `
    <!DOCTYPE html>
    <html>
    <head>
      <style>
        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #111827; max-width: 600px; margin: 0 auto; background-color: #f9fafb; }
        .container { padding: 32px 24px; background-color: #ffffff; border-radius: 12px; margin: 20px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); border: 1px solid #e5e7eb; }
        .header { margin-bottom: 24px; }
        .logo { font-size: 24px; font-weight: 700; color: #111827; margin-bottom: 4px; }
        h1 { color: #111827; font-size: 20px; font-weight: 600; margin-top: 0; margin-bottom: 16px; }
        p { color: #4b5563; margin-bottom: 24px; font-size: 16px; }
        .button { display: inline-block; background-color: #22c55e; color: white; text-decoration: none; padding: 10px 20px; border-radius: 6px; font-weight: 500; margin-top: 8px; margin-bottom: 24px; }
        .button:hover { background-color: #16a34a; }
        .footer { margin-top: 32px; padding-top: 16px; border-top: 1px solid #e5e7eb; font-size: 14px; color: #6b7280; }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <div class="logo">Hivemind</div>
        </div>
        <h1>Reset Your Password</h1>
        <p>We received a request to reset your password. Click the button below to create a new password:</p>
        <a href="${url}" class="button">Reset Password</a>
        <p style="margin-top: 24px;">If you didn't request this password reset, you can safely ignore this email.</p>
        <div class="footer">
          <p style="margin-bottom: 8px;">© ${new Date().getFullYear()} Hivemind. All rights reserved.</p>
        </div>
      </div>
    </body>
    </html>
  `
});