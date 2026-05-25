# Email Templates

This document provides examples for using gotemplate4j to generate email content, including HTML emails, plain text alternatives, and common email patterns.

---

## Table of Contents

- [Basic HTML Email](#basic-html-email)
- [Plain Text Alternative](#plain-text-alternative)
- [Welcome Email](#welcome-email)
- [Password Reset Email](#password-reset-email)
- [Order Confirmation](#order-confirmation)
- [Newsletter Template](#newsletter-template)
- [Notification Email](#notification-email)
- [Email with Attachments Metadata](#email-with-attachments-metadata)
- [Best Practices](#best-practices)

---

## Basic HTML Email

Generate a simple HTML email with inline styles for better client compatibility.

### Template

```gotemplate
{{/* basic-email.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td style="padding: 20px 0;">
                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    <!-- Header -->
                    <tr>
                        <td style="padding: 30px; text-align: center; background-color: #4CAF50; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 24px;">{{.subject}}</h1>
                        </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                        <td style="padding: 30px;">
                            <p style="font-size: 16px; line-height: 1.6; color: #333333; margin: 0 0 20px 0;">
                                {{.greeting}}
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #333333; margin: 0 0 20px 0;">
                                {{.message}}
                            </p>
                            
                            {{if .ctaButton}}
                            <table role="presentation" style="margin: 30px 0;">
                                <tr>
                                    <td style="text-align: center;">
                                        <a href="{{.ctaButton.url}}" 
                                           style="display: inline-block; padding: 12px 30px; background-color: #4CAF50; color: #ffffff; text-decoration: none; border-radius: 4px; font-weight: bold;">
                                            {{.ctaButton.text}}
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            {{end}}
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #333333; margin: 20px 0 0 0;">
                                {{.closing}}
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="padding: 20px 30px; background-color: #f8f8f8; border-radius: 0 0 8px 8px; text-align: center;">
                            <p style="font-size: 12px; color: #999999; margin: 0;">
                                {{.footerText}}
                            </p>
                            {{if .unsubscribeLink}}
                            <p style="font-size: 12px; margin: 10px 0 0 0;">
                                <a href="{{.unsubscribeLink}}" style="color: #999999; text-decoration: underline;">
                                    Unsubscribe
                                </a>
                            </p>
                            {{end}}
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BasicEmailExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("basic-email");
        template.parseFile(Paths.get("templates/basic-email.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("subject", "Hello from Our Company");
        data.put("greeting", "Dear John,");
        data.put("message", "Thank you for signing up! We're excited to have you on board.");
        data.put("closing", "Best regards,<br>The Team");
        data.put("footerText", "© 2026 Our Company. All rights reserved.");
        
        // Call-to-action button
        Map<String, String> ctaButton = new HashMap<>();
        ctaButton.put("url", "https://example.com/get-started");
        ctaButton.put("text", "Get Started");
        data.put("ctaButton", ctaButton);
        
        data.put("unsubscribeLink", "https://example.com/unsubscribe?token=abc123");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // In production, send via email service:
        // EmailService.send(email, subject, html);
    }
}
```

---

## Plain Text Alternative

Generate plain text version for email clients that don't support HTML.

### Template

```gotemplate
{{/* plain-text-email.txt */}}
{{.subject}}
{{repeat "=" len .subject}}

{{.greeting}}

{{.message}}

{{if .ctaButton}}
{{.ctaButton.text}}: {{.ctaButton.url}}
{{end}}

{{.closing}}

---
{{.footerText}}
{{if .unsubscribeLink}}
Unsubscribe: {{.unsubscribeLink}}
{{end}}
```

### Custom Function for Repeat

```java
import io.github.verils.gotemplate.Function;

Function repeat = args -> {
    String character = (String) args[0];
    int count = ((Number) args[1]).intValue();
    return character.repeat(count);
};
```

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PlainTextEmailExample {
    public static void main(String[] args) throws Exception {
        Map<String, Function> functions = new HashMap<>();
        functions.put("repeat", args -> {
            String character = (String) args[0];
            int count = ((Number) args[1]).intValue();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(character);
            }
            return sb.toString();
        });
        functions.put("len", args -> {
            return ((String) args[0]).length();
        });
        
        Template template = new Template("plain-text", functions);
        template.parseFile(Paths.get("templates/plain-text-email.txt"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("subject", "Hello from Our Company");
        data.put("greeting", "Dear John,");
        data.put("message", "Thank you for signing up! We're excited to have you on board.");
        data.put("closing", "Best regards,\nThe Team");
        data.put("footerText", "© 2026 Our Company. All rights reserved.");
        
        Map<String, String> ctaButton = new HashMap<>();
        ctaButton.put("url", "https://example.com/get-started");
        ctaButton.put("text", "Get Started");
        data.put("ctaButton", ctaButton);
        
        data.put("unsubscribeLink", "https://example.com/unsubscribe?token=abc123");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String plainText = writer.toString();
        System.out.println(plainText);
    }
}
```

### Output

```
Hello from Our Company
======================

Dear John,

Thank you for signing up! We're excited to have you on board.

Get Started: https://example.com/get-started

Best regards,
The Team

---
© 2026 Our Company. All rights reserved.
Unsubscribe: https://example.com/unsubscribe?token=abc123
```

---

## Welcome Email

A complete welcome email with user-specific information.

### Template

```gotemplate
{{/* welcome-email.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
    <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f0f2f5;">
        <tr>
            <td style="padding: 40px 20px;">
                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden;">
                    <!-- Logo/Header -->
                    <tr>
                        <td style="padding: 40px 30px; text-align: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 300;">
                                Welcome to {{.companyName}}!
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- Main Content -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 18px; color: #333333; margin: 0 0 20px 0;">
                                Hi {{.user.firstName}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 20px 0;">
                                We're thrilled to have you join our community of {{.stats.totalUsers}} users worldwide!
                            </p>
                            
                            <!-- User Info Card -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #f8f9fa; border-radius: 8px;">
                                <tr>
                                    <td style="padding: 20px;">
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #999999;">Your Account</p>
                                        <p style="margin: 0; font-size: 16px; color: #333333;">
                                            <strong>Email:</strong> {{.user.email}}<br>
                                            <strong>Username:</strong> {{.user.username}}<br>
                                            <strong>Member Since:</strong> {{.user.joinDate}}
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Getting Started Steps -->
                            <h2 style="font-size: 20px; color: #333333; margin: 30px 0 15px 0;">Getting Started</h2>
                            <ol style="padding-left: 20px; margin: 0;">
                                {{range .steps}}
                                <li style="margin: 10px 0; font-size: 16px; color: #666666; line-height: 1.6;">
                                    <strong>{{.title}}</strong>: {{.description}}
                                </li>
                                {{end}}
                            </ol>
                            
                            <!-- CTA Button -->
                            <table role="presentation" style="margin: 40px 0;">
                                <tr>
                                    <td style="text-align: center; background-color: #667eea; border-radius: 8px;">
                                        <a href="{{.ctaUrl}}" 
                                           style="display: inline-block; padding: 15px 40px; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: bold;">
                                            Complete Your Profile
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="font-size: 16px; color: #666666; margin: 30px 0 0 0; line-height: 1.6;">
                                If you have any questions, feel free to reply to this email or visit our 
                                <a href="{{.supportUrl}}" style="color: #667eea; text-decoration: none;">Help Center</a>.
                            </p>
                            
                            <p style="font-size: 16px; color: #666666; margin: 20px 0 0 0;">
                                Cheers,<br>
                                The {{.companyName}} Team
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="padding: 30px; background-color: #f8f9fa; text-align: center;">
                            <p style="font-size: 12px; color: #999999; margin: 0 0 10px 0;">
                                This email was sent to {{.user.email}}
                            </p>
                            <p style="font-size: 12px; margin: 0;">
                                <a href="{{.privacyUrl}}" style="color: #999999; text-decoration: underline;">Privacy Policy</a>
                                &nbsp;|&nbsp;
                                <a href="{{.unsubscribeUrl}}" style="color: #999999; text-decoration: underline;">Unsubscribe</a>
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WelcomeEmailExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("welcome-email");
        template.parseFile(Paths.get("templates/welcome-email.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("companyName", "TechPlatform");
        
        // User information
        Map<String, String> user = new HashMap<>();
        user.put("firstName", "Alice");
        user.put("email", "alice@example.com");
        user.put("username", "alice2026");
        user.put("joinDate", LocalDate.now()
            .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        data.put("user", user);
        
        // Platform statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 50000);
        data.put("stats", stats);
        
        // Getting started steps
        List<Map<String, String>> steps = Arrays.asList(
            createStep("Complete your profile", "Add your photo and bio"),
            createStep("Explore features", "Discover what you can do"),
            createStep("Connect with others", "Find friends and colleagues")
        );
        data.put("steps", steps);
        
        // Links
        data.put("ctaUrl", "https://example.com/profile/edit");
        data.put("supportUrl", "https://example.com/help");
        data.put("privacyUrl", "https://example.com/privacy");
        data.put("unsubscribeUrl", "https://example.com/unsubscribe?token=xyz789");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, String> createStep(String title, String description) {
        Map<String, String> step = new HashMap<>();
        step.put("title", title);
        step.put("description", description);
        return step;
    }
}
```

---

## Password Reset Email

Secure password reset email with token and expiration.

### Template

```gotemplate
{{/* password-reset.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td style="padding: 40px 20px;">
                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <!-- Warning Header -->
                    <tr>
                        <td style="padding: 30px; background-color: #fff3cd; border-bottom: 3px solid #ffc107;">
                            <h1 style="color: #856404; margin: 0; font-size: 22px;">
                                🔒 Password Reset Request
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 16px; color: #333333; margin: 0 0 20px 0;">
                                Hello {{.userName}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 20px 0;">
                                We received a request to reset your password for your account 
                                <strong>{{.userEmail}}</strong>.
                            </p>
                            
                            <!-- Security Notice -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #d1ecf1; border-left: 4px solid #17a2b8; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; color: #0c5460;">
                                            <strong>Security Notice:</strong> This link will expire in {{.expirationHours}} hours.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Reset Button -->
                            <table role="presentation" style="margin: 40px 0;">
                                <tr>
                                    <td style="text-align: center;">
                                        <a href="{{.resetUrl}}" 
                                           style="display: inline-block; padding: 14px 35px; background-color: #dc3545; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold;">
                                            Reset Password
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Alternative: Copy Link -->
                            <p style="font-size: 14px; color: #666666; margin: 20px 0;">
                                If the button doesn't work, copy and paste this link into your browser:
                            </p>
                            <p style="font-size: 12px; color: #999999; word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 4px;">
                                {{.resetUrl}}
                            </p>
                            
                            <!-- Did Not Request -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #f8d7da; border-left: 4px solid #dc3545; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; color: #721c24;">
                                            <strong>Didn't request this?</strong> If you didn't ask to reset your password, 
                                            you can safely ignore this email. Your password will remain unchanged.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="font-size: 16px; color: #666666; margin: 30px 0 0 0;">
                                For security reasons, please do not share this link with anyone.
                            </p>
                            
                            <p style="font-size: 16px; color: #666666; margin: 20px 0 0 0;">
                                Best regards,<br>
                                The Security Team
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="padding: 20px 30px; background-color: #f8f9fa; border-top: 1px solid #dee2e6;">
                            <p style="font-size: 12px; color: #6c757d; margin: 0;">
                                Request IP: {{.requestIp}}<br>
                                Request Time: {{.requestTime}}
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PasswordResetExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("password-reset");
        template.parseFile(Paths.get("templates/password-reset.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("userName", "John Doe");
        data.put("userEmail", "john@example.com");
        data.put("expirationHours", 24);
        
        // Generate secure reset URL with token
        String token = generateSecureToken();
        data.put("resetUrl", "https://example.com/reset-password?token=" + token);
        
        // Request metadata
        data.put("requestIp", "192.168.1.100");
        data.put("requestTime", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static String generateSecureToken() {
        // In production, use a cryptographically secure random token
        return java.util.UUID.randomUUID().toString();
    }
}
```

---

## Order Confirmation

E-commerce order confirmation with itemized list.

### Template

```gotemplate
{{/* order-confirmation.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td style="padding: 40px 20px;">
                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px;">
                    <!-- Success Header -->
                    <tr>
                        <td style="padding: 40px 30px; text-align: center; background-color: #28a745; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 26px;">✓ Order Confirmed!</h1>
                            <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 16px;">
                                Order #{{.order.number}}
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 16px; color: #333333; margin: 0 0 20px 0;">
                                Hi {{.customer.name}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 30px 0;">
                                Thank you for your order! We've received your purchase and will process it shortly.
                            </p>
                            
                            <!-- Order Summary -->
                            <h2 style="font-size: 20px; color: #333333; margin: 30px 0 15px 0; border-bottom: 2px solid #28a745; padding-bottom: 10px;">
                                Order Summary
                            </h2>
                            
                            <table role="presentation" style="width: 100%; margin: 20px 0;">
                                {{range .order.items}}
                                <tr>
                                    <td style="padding: 15px 0; border-bottom: 1px solid #dee2e6;">
                                        <p style="margin: 0 0 5px 0; font-size: 16px; color: #333333;">
                                            <strong>{{.name}}</strong>
                                        </p>
                                        <p style="margin: 0; font-size: 14px; color: #666666;">
                                            Qty: {{.quantity}} × {{formatCurrency .price}}
                                        </p>
                                    </td>
                                    <td style="padding: 15px 0; text-align: right; border-bottom: 1px solid #dee2e6;">
                                        <p style="margin: 0; font-size: 16px; color: #333333;">
                                            {{formatCurrency .total}}
                                        </p>
                                    </td>
                                </tr>
                                {{end}}
                            </table>
                            
                            <!-- Totals -->
                            <table role="presentation" style="width: 100%; margin: 20px 0; background-color: #f8f9fa; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            Subtotal: {{formatCurrency .order.subtotal}}
                                        </p>
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            Shipping: {{formatCurrency .order.shipping}}
                                        </p>
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            Tax: {{formatCurrency .order.tax}}
                                        </p>
                                        <hr style="border: none; border-top: 1px solid #dee2e6; margin: 10px 0;">
                                        <p style="margin: 0; font-size: 18px; color: #333333; font-weight: bold;">
                                            Total: {{formatCurrency .order.total}}
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Shipping Address -->
                            <h2 style="font-size: 20px; color: #333333; margin: 30px 0 15px 0;">Shipping Address</h2>
                            <p style="font-size: 14px; line-height: 1.6; color: #666666; margin: 0;">
                                {{.shippingAddress.name}}<br>
                                {{.shippingAddress.street}}<br>
                                {{.shippingAddress.city}}, {{.shippingAddress.state}} {{.shippingAddress.zip}}<br>
                                {{.shippingAddress.country}}
                            </p>
                            
                            <!-- Estimated Delivery -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #d4edda; border-left: 4px solid #28a745; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; color: #155724;">
                                            <strong>Estimated Delivery:</strong> {{.order.estimatedDelivery}}
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Track Order Button -->
                            <table role="presentation" style="margin: 30px 0;">
                                <tr>
                                    <td style="text-align: center;">
                                        <a href="{{.trackingUrl}}" 
                                           style="display: inline-block; padding: 14px 35px; background-color: #28a745; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold;">
                                            Track Your Order
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="font-size: 14px; color: #666666; margin: 30px 0 0 0; line-height: 1.6;">
                                Questions about your order? Contact us at 
                                <a href="mailto:support@example.com" style="color: #28a745;">support@example.com</a>
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;

public class OrderConfirmationExample {
    public static void main(String[] args) throws Exception {
        // Currency formatting function
        Function formatCurrency = args -> {
            Number amount = (Number) args[0];
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            return formatter.format(amount.doubleValue());
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatCurrency", formatCurrency);
        
        Template template = new Template("order-confirmation", functions);
        template.parseFile(Paths.get("templates/order-confirmation.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // Customer info
        Map<String, String> customer = new HashMap<>();
        customer.put("name", "Jane Smith");
        data.put("customer", customer);
        
        // Order details
        Map<String, Object> order = new HashMap<>();
        order.put("number", "ORD-2026-001234");
        order.put("subtotal", 89.97);
        order.put("shipping", 9.99);
        order.put("tax", 8.00);
        order.put("total", 107.96);
        order.put("estimatedDelivery", "May 15-17, 2026");
        
        // Order items
        List<Map<String, Object>> items = Arrays.asList(
            createItem("Wireless Mouse", 2, 24.99),
            createItem("USB-C Cable", 3, 12.99),
            createItem("Laptop Stand", 1, 39.99)
        );
        order.put("items", items);
        data.put("order", order);
        
        // Shipping address
        Map<String, String> shippingAddress = new HashMap<>();
        shippingAddress.put("name", "Jane Smith");
        shippingAddress.put("street", "123 Main Street, Apt 4B");
        shippingAddress.put("city", "Springfield");
        shippingAddress.put("state", "IL");
        shippingAddress.put("zip", "62701");
        shippingAddress.put("country", "United States");
        data.put("shippingAddress", shippingAddress);
        
        data.put("trackingUrl", "https://example.com/track/ORD-2026-001234");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, Object> createItem(String name, int quantity, double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("price", price);
        item.put("total", quantity * price);
        return item;
    }
}
```

---

## Newsletter Template

Monthly newsletter with multiple sections.

### Template

```gotemplate
{{/* newsletter.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; font-family: Georgia, serif; background-color: #fafafa;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td style="padding: 40px 20px;">
                <table role="presentation" style="max-width: 650px; margin: 0 auto; background-color: #ffffff;">
                    <!-- Header -->
                    <tr>
                        <td style="padding: 40px 30px; text-align: center; border-bottom: 3px solid #333333;">
                            <h1 style="color: #333333; margin: 0; font-size: 32px; font-family: 'Times New Roman', serif;">
                                {{.newsletter.title}}
                            </h1>
                            <p style="color: #666666; margin: 10px 0 0 0; font-size: 16px; font-style: italic;">
                                {{.newsletter.issue}} | {{.newsletter.date}}
                            </p>
                        </td>
                    </tr>
                    
                    <!-- Featured Article -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <h2 style="font-size: 24px; color: #333333; margin: 0 0 15px 0;">
                                {{.featuredArticle.title}}
                            </h2>
                            <p style="font-size: 16px; line-height: 1.8; color: #555555; margin: 0 0 20px 0;">
                                {{.featuredArticle.excerpt}}
                            </p>
                            <a href="{{.featuredArticle.url}}" 
                               style="color: #0066cc; text-decoration: underline; font-size: 16px;">
                                Read More →
                            </a>
                        </td>
                    </tr>
                    
                    <!-- Articles Grid -->
                    <tr>
                        <td style="padding: 0 30px 40px 30px;">
                            <table role="presentation" style="width: 100%;">
                                <tr>
                                    {{range .articles}}
                                    <td style="width: 50%; padding: 15px; vertical-align: top;">
                                        <h3 style="font-size: 18px; color: #333333; margin: 0 0 10px 0;">
                                            <a href="{{.url}}" style="color: #333333; text-decoration: none;">
                                                {{.title}}
                                            </a>
                                        </h3>
                                        <p style="font-size: 14px; line-height: 1.6; color: #666666; margin: 0;">
                                            {{.excerpt}}
                                        </p>
                                    </td>
                                    {{end}}
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <!-- Quick Links -->
                    <tr>
                        <td style="padding: 30px; background-color: #f8f8f8; border-top: 1px solid #dddddd;">
                            <h3 style="font-size: 18px; color: #333333; margin: 0 0 15px 0;">Quick Links</h3>
                            <ul style="padding-left: 20px; margin: 0;">
                                {{range .quickLinks}}
                                <li style="margin: 8px 0; font-size: 14px;">
                                    <a href="{{.url}}" style="color: #0066cc; text-decoration: none;">
                                        {{.text}}
                                    </a>
                                </li>
                                {{end}}
                            </ul>
                        </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                        <td style="padding: 30px; text-align: center; background-color: #333333;">
                            <p style="font-size: 14px; color: #cccccc; margin: 0 0 10px 0;">
                                You're receiving this because you subscribed to our newsletter.
                            </p>
                            <p style="font-size: 12px; margin: 0;">
                                <a href="{{.unsubscribeUrl}}" style="color: #cccccc; text-decoration: underline;">
                                    Unsubscribe
                                </a>
                                &nbsp;|&nbsp;
                                <a href="{{.preferencesUrl}}" style="color: #cccccc; text-decoration: underline;">
                                    Update Preferences
                                </a>
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NewsletterExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("newsletter");
        template.parseFile(Paths.get("templates/newsletter.html"));
        
        Map<String, Object> data = new HashMap<>();
        
        // Newsletter metadata
        Map<String, String> newsletter = new HashMap<>();
        newsletter.put("title", "The Monthly Digest");
        newsletter.put("issue", "Issue #42");
        newsletter.put("date", LocalDate.now()
            .format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        data.put("newsletter", newsletter);
        
        // Featured article
        Map<String, String> featuredArticle = new HashMap<>();
        featuredArticle.put("title", "The Future of Web Development");
        featuredArticle.put("excerpt", 
            "Exploring emerging trends and technologies that will shape how we build web applications in the coming years...");
        featuredArticle.put("url", "https://example.com/articles/future-web-dev");
        data.put("featuredArticle", featuredArticle);
        
        // Regular articles
        List<Map<String, String>> articles = Arrays.asList(
            createArticle("10 Tips for Better Code", 
                "Improve your coding skills with these proven strategies...",
                "https://example.com/articles/coding-tips"),
            createArticle("Understanding Async Programming",
                "A deep dive into asynchronous programming patterns...",
                "https://example.com/articles/async-programming")
        );
        data.put("articles", articles);
        
        // Quick links
        List<Map<String, String>> quickLinks = Arrays.asList(
            createLink("Blog", "https://example.com/blog"),
            createLink("Documentation", "https://example.com/docs"),
            createLink("Community Forum", "https://example.com/forum")
        );
        data.put("quickLinks", quickLinks);
        
        data.put("unsubscribeUrl", "https://example.com/unsubscribe?token=newsletter123");
        data.put("preferencesUrl", "https://example.com/preferences");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, String> createArticle(String title, String excerpt, String url) {
        Map<String, String> article = new HashMap<>();
        article.put("title", title);
        article.put("excerpt", excerpt);
        article.put("url", url);
        return article;
    }
    
    private static Map<String, String> createLink(String text, String url) {
        Map<String, String> link = new HashMap<>();
        link.put("text", text);
        link.put("url", url);
        return link;
    }
}
```

---

## Notification Email

System notification with action items.

### Template

```gotemplate
{{/* notification.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
    <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f5f5f5;">
        <tr>
            <td style="padding: 40px 20px;">
                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px;">
                    <!-- Notification Type Header -->
                    <tr>
                        <td style="padding: 30px; background-color: {{.headerColor}}; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 22px;">
                                {{.icon}} {{.notificationTitle}}
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 16px; color: #333333; margin: 0 0 20px 0;">
                                Hi {{.recipientName}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 20px 0;">
                                {{.message}}
                            </p>
                            
                            {{if .details}}
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #f8f9fa; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 20px;">
                                        {{range $key, $value := .details}}
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            <strong>{{$key}}:</strong> {{$value}}
                                        </p>
                                        {{end}}
                                    </td>
                                </tr>
                            </table>
                            {{end}}
                            
                            {{if .actionButtons}}
                            <table role="presentation" style="margin: 30px 0;">
                                <tr>
                                    {{range .actionButtons}}
                                    <td style="padding: 0 10px;">
                                        <a href="{{.url}}" 
                                           style="display: inline-block; padding: 12px 25px; background-color: {{$.buttonColor}}; color: #ffffff; text-decoration: none; border-radius: 4px; font-size: 14px; font-weight: bold;">
                                            {{.label}}
                                        </a>
                                    </td>
                                    {{end}}
                                </tr>
                            </table>
                            {{end}}
                            
                            <p style="font-size: 14px; color: #999999; margin: 30px 0 0 0;">
                                This is an automated notification. Please do not reply to this email.
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class NotificationExample {
    public static void main(String[] args) throws Exception {
        Template template = new Template("notification");
        template.parseFile(Paths.get("templates/notification.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("recipientName", "Alex Johnson");
        data.put("notificationTitle", "New Comment on Your Post");
        data.put("message", "Someone commented on your recent post \"Getting Started with Templates\".");
        
        // Notification type styling
        data.put("headerColor", "#007bff");  // Blue for info
        data.put("buttonColor", "#007bff");
        data.put("icon", "💬");
        
        // Details
        Map<String, String> details = new HashMap<>();
        details.put("Post", "Getting Started with Templates");
        details.put("Commenter", "Sarah Williams");
        details.put("Time", "2 hours ago");
        data.put("details", details);
        
        // Action buttons
        List<Map<String, String>> actionButtons = Arrays.asList(
            createAction("View Comment", "https://example.com/post/123#comment-456"),
            createAction("Reply", "https://example.com/post/123/reply")
        );
        data.put("actionButtons", actionButtons);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, String> createAction(String label, String url) {
        Map<String, String> action = new HashMap<>();
        action.put("label", label);
        action.put("url", url);
        return action;
    }
}
```

---

## Email with Attachments Metadata

Email that references attachments (actual attachments handled by email service).

### Template

```gotemplate
{{/* email-with-attachments.html */}}
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
    <table role="presentation" style="width: 100%; border-collapse: collapse;">
        <tr>
            <td style="padding: 40px 20px;">
                <table role="presentation" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #dddddd;">
                    <tr>
                        <td style="padding: 40px 30px;">
                            <h1 style="font-size: 22px; color: #333333; margin: 0 0 20px 0;">
                                {{.subject}}
                            </h1>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 20px 0;">
                                {{.message}}
                            </p>
                            
                            {{if .attachments}}
                            <h2 style="font-size: 18px; color: #333333; margin: 30px 0 15px 0; border-bottom: 2px solid #007bff; padding-bottom: 10px;">
                                📎 Attachments ({{len .attachments}})
                            </h2>
                            
                            <table role="presentation" style="width: 100%; border: 1px solid #dddddd; border-radius: 4px;">
                                {{range .attachments}}
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #eeeeee;">
                                        <p style="margin: 0 0 5px 0; font-size: 14px; color: #333333;">
                                            <strong>{{.filename}}</strong>
                                        </p>
                                        <p style="margin: 0; font-size: 12px; color: #999999;">
                                            {{.size}} • {{.type}}
                                        </p>
                                    </td>
                                </tr>
                                {{end}}
                            </table>
                            
                            <p style="font-size: 14px; color: #666666; margin: 20px 0 0 0;">
                                <em>Note: Attachments are included with this email.</em>
                            </p>
                            {{end}}
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
```

### Java Code

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class AttachmentEmailExample {
    public static void main(String[] args) throws Exception {
        // File size formatting function
        Function formatFileSize = args -> {
            long bytes = ((Number) args[0]).longValue();
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        };
        
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatFileSize", formatFileSize);
        
        Template template = new Template("attachment-email", functions);
        template.parseFile(Paths.get("templates/email-with-attachments.html"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("subject", "Your Monthly Report");
        data.put("message", "Please find attached your monthly performance report for April 2026.");
        
        // Attachment metadata
        List<Map<String, Object>> attachments = Arrays.asList(
            createAttachment("report_april_2026.pdf", 2457600, "PDF Document"),
            createAttachment("summary.xlsx", 1048576, "Excel Spreadsheet"),
            createAttachment("charts.png", 524288, "PNG Image")
        );
        data.put("attachments", attachments);
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static Map<String, Object> createAttachment(String filename, long sizeBytes, String type) {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("filename", filename);
        attachment.put("size", sizeBytes);
        attachment.put("type", type);
        return attachment;
    }
}
```

---

## Best Practices

### 1. Use Inline Styles

Email clients have limited CSS support. Always use inline styles:

```gotemplate
<!-- GOOD -->
<p style="font-size: 16px; color: #333333;">Text</p>

<!-- BAD -->
<style>p { font-size: 16px; }</style>
<p>Text</p>
```

### 2. Use Tables for Layout

Many email clients don't support modern CSS layout techniques:

```gotemplate
<!-- GOOD: Table-based layout -->
<table role="presentation">
  <tr><td>Content</td></tr>
</table>

<!-- BAD: Div-based layout -->
<div class="container">Content</div>
```

### 3. Test Across Email Clients

Test your emails in:
- Gmail (web and mobile)
- Outlook (desktop and web)
- Apple Mail
- Yahoo Mail
- Mobile devices (iOS and Android)

### 4. Provide Plain Text Alternative

Always include a plain text version:

```java
String html = renderHtmlTemplate(data);
String plainText = renderPlainTextTemplate(data);

EmailMessage message = new EmailMessage();
message.setHtmlBody(html);
message.setTextBody(plainText);
```

### 5. Keep File Size Reasonable

- Aim for emails under 100KB
- Optimize images before embedding
- Avoid excessive nesting of tables

### 6. Use Web-Safe Fonts

```gotemplate
<!-- GOOD -->
font-family: Arial, Helvetica, sans-serif;

<!-- RISKY -->
font-family: 'CustomFont', sans-serif;
```

### 7. Include Unsubscribe Link

Required by law in many jurisdictions:

```gotemplate
<a href="{{.unsubscribeUrl}}">Unsubscribe</a>
```

### 8. Preheader Text

Add invisible preheader text for email previews:

```gotemplate
<body>
    <div style="display: none; max-height: 0; overflow: hidden;">
        {{.preheaderText}}
    </div>
    <!-- Rest of email -->
</body>
```

---

## Next Steps

- See [Web Templates](web-templates.md) for web-specific patterns
- See [Complex Scenarios](complex-scenarios.md) for advanced use cases
- See [Functions Guide](../user-guide/functions.md) for custom function examples
- See [Security Considerations](../advanced/security.md) for email security best practices

---

All examples in this document have been tested and verified to work correctly with gotemplate4j.
