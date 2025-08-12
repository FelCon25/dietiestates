import { Resend } from 'resend';

const resend = new Resend(process.env.RESEND_API_KEY);

type Params = Readonly<{
  to: string;
  subject: string;
  text: string;
  html: string;
}>;

const getFromEmail = () =>
  process.env.NODE_ENV === 'development' ? 'onboarding@resend.dev' : process.env.EMAIL_SENDER;

const getToEmail = (to: string) =>
  process.env.NODE_ENV === 'development' ? 'delivered@resend.dev' : to;

export const sendMail = async (params: Params) => {
  const { to, subject, text, html } = params;

  if (!to || !subject || !text || !html) {
    throw new Error('Missing required email parameters');
  }

  try {
    const response = await resend.emails.send({
      from: getFromEmail() || "",
      to: getToEmail(to),
      subject,
      text,
      html,
    });
    return { success: true, response };
  } catch (error) {
    console.error('Resend error:', error);
    return { success: false, error };
  }
};