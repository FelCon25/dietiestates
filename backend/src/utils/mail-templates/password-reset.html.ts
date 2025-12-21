export const getPasswordResetTemplate = (code: string) => ({
  subject: "Password Reset Code - DietiEstates",
  text: `You requested a password reset. Use this code to reset your password: ${code}. The code will expire in 15 minutes.`,
  html: `
    <!DOCTYPE html>
    <html>
    <head>
      <style>
        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #111827; max-width: 600px; margin: 0 auto; background-color: #f9fafb; }
        .container { padding: 32px 24px; background-color: #ffffff; border-radius: 12px; margin: 20px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); border: 1px solid #e5e7eb; }
        .header { margin-bottom: 24px; }
        .logo { font-size: 24px; font-weight: 700; color: #22c55e; margin-bottom: 4px; }
        h1 { color: #111827; font-size: 20px; font-weight: 600; margin-top: 0; margin-bottom: 16px; }
        p { color: #4b5563; margin-bottom: 24px; font-size: 16px; }
        .code-box { background-color: #f3f4f6; border: 2px solid #22c55e; border-radius: 8px; padding: 20px; text-align: center; margin: 24px 0; }
        .code { font-size: 32px; font-weight: 700; color: #111827; letter-spacing: 8px; font-family: 'Courier New', monospace; }
        .warning { background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px 16px; margin: 24px 0; border-radius: 4px; }
        .warning p { margin: 0; color: #92400e; font-size: 14px; }
        .footer { margin-top: 32px; padding-top: 16px; border-top: 1px solid #e5e7eb; font-size: 14px; color: #6b7280; }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <div class="logo">DietiEstates</div>
        </div>
        <h1>Password Reset</h1>
        <p>We received a request to reset your password. Use the code below to create a new password:</p>
        <div class="code-box">
          <div class="code">${code}</div>
        </div>
        <div class="warning">
          <p><strong>⚠️ Important:</strong> This code will expire in 15 minutes.</p>
        </div>
        <p style="margin-top: 24px;">If you didn't request a password reset, you can safely ignore this email.</p>
        <div class="footer">
          <p style="margin-bottom: 8px;">© ${new Date().getFullYear()} DietiEstates. All rights reserved.</p>
        </div>
      </div>
    </body>
    </html>
  `
});