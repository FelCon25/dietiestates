export const getAccountCreatedTemplate = (email: string, password: string) => ({
    subject: "Il tuo account è stato creato",
    text: `Il tuo account è stato creato.\nEmail: ${email}\nPassword: ${password}\nAccedi e cambia la password dopo il primo accesso.`,
    html: `
    <div style="font-family: Arial, sans-serif; max-width: 600px;">
      <h2>Il tuo account è stato creato</h2>
      <p>Puoi accedere con queste credenziali:</p>
      <ul>
        <li><strong>Email:</strong> ${email}</li>
        <li><strong>Password:</strong> ${password}</li>
      </ul>
      <p>Ti consigliamo di cambiare la password dopo il primo accesso.</p>
    </div>
  `
});