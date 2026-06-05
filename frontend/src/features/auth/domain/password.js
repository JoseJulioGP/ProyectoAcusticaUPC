/**
 * Reglas de contraseña del registro:
 * ≥ 10 caracteres, una mayúscula, una minúscula, un dígito.
 */

export function passwordChecks(pw = "") {
  return {
    length: pw.length >= 10,
    upper: /[A-Z]/.test(pw),
    lower: /[a-z]/.test(pw),
    digit: /\d/.test(pw),
  };
}

export function isPasswordValid(pw = "") {
  const c = passwordChecks(pw);
  return c.length && c.upper && c.lower && c.digit;
}
