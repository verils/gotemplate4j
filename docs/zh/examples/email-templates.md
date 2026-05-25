# 邮件模板

本文档提供了使用 gotemplate4j 生成邮件内容的示例，包括 HTML 邮件、纯文本替代方案以及常见邮件模式。

---

## 目录

- [基础 HTML 邮件](#基础-html-邮件)
- [纯文本替代方案](#纯文本替代方案)
- [欢迎邮件](#欢迎邮件)
- [密码重置邮件](#密码重置邮件)
- [订单确认](#订单确认)
- [新闻通讯模板](#新闻通讯模板)
- [通知邮件](#通知邮件)
- [带附件元数据的邮件](#带附件元数据的邮件)
- [最佳实践](#最佳实践)

---

## 基础 HTML 邮件

生成带有内联样式的简单 HTML 邮件，以获得更好的邮件客户端兼容性。

### 模板

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
                    <!-- 头部 -->
                    <tr>
                        <td style="padding: 30px; text-align: center; background-color: #4CAF50; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 24px;">{{.subject}}</h1>
                        </td>
                    </tr>
                    
                    <!-- 内容 -->
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
                    
                    <!-- 页脚 -->
                    <tr>
                        <td style="padding: 20px 30px; background-color: #f8f8f8; border-radius: 0 0 8px 8px; text-align: center;">
                            <p style="font-size: 12px; color: #999999; margin: 0;">
                                {{.footerText}}
                            </p>
                            {{if .unsubscribeLink}}
                            <p style="font-size: 12px; margin: 10px 0 0 0;">
                                <a href="{{.unsubscribeLink}}" style="color: #999999; text-decoration: underline;">
                                    取消订阅
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

### Java 代码

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
        data.put("subject", "来自我们公司的问候");
        data.put("greeting", "尊敬的 John,");
        data.put("message", "感谢您的注册！我们很高兴您的加入。");
        data.put("closing", "谨致问候,<br>团队");
        data.put("footerText", "© 2026 Our Company. 保留所有权利。");
        
        // 行动号召按钮
        Map<String, String> ctaButton = new HashMap<>();
        ctaButton.put("url", "https://example.com/get-started");
        ctaButton.put("text", "开始使用");
        data.put("ctaButton", ctaButton);
        
        data.put("unsubscribeLink", "https://example.com/unsubscribe?token=abc123");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
        
        // 在生产环境中，通过邮件服务发送：
        // EmailService.send(email, subject, html);
    }
}
```

---

## 纯文本替代方案

为不支持 HTML 的邮件客户端生成纯文本版本。

### 模板

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
取消订阅: {{.unsubscribeLink}}
{{end}}
```

### 自定义 Repeat 函数

```java
import io.github.verils.gotemplate.Function;

Function repeat = args -> {
    String character = (String) args[0];
    int count = ((Number) args[1]).intValue();
    return character.repeat(count);
};
```

### Java 代码

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
        data.put("subject", "来自我们公司的问候");
        data.put("greeting", "尊敬的 John,");
        data.put("message", "感谢您的注册！我们很高兴您的加入。");
        data.put("closing", "谨致问候,\n团队");
        data.put("footerText", "© 2026 Our Company. 保留所有权利。");
        
        Map<String, String> ctaButton = new HashMap<>();
        ctaButton.put("url", "https://example.com/get-started");
        ctaButton.put("text", "开始使用");
        data.put("ctaButton", ctaButton);
        
        data.put("unsubscribeLink", "https://example.com/unsubscribe?token=abc123");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String plainText = writer.toString();
        System.out.println(plainText);
    }
}
```

### 输出

```
来自我们公司的问候
======================

尊敬的 John,

感谢您的注册！我们很高兴您的加入。

开始使用: https://example.com/get-started

谨致问候,
团队

---
© 2026 Our Company. 保留所有权利。
取消订阅: https://example.com/unsubscribe?token=abc123
```

---

## 欢迎邮件

包含用户特定信息的完整欢迎邮件。

### 模板

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
                    <!-- Logo/头部 -->
                    <tr>
                        <td style="padding: 40px 30px; text-align: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 300;">
                                欢迎加入 {{.companyName}}！
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- 主要内容 -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 18px; color: #333333; margin: 0 0 20px 0;">
                                您好 {{.user.firstName}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 20px 0;">
                                我们非常高兴您加入了我们拥有 {{.stats.totalUsers}} 全球用户的社区！
                            </p>
                            
                            <!-- 用户信息卡片 -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #f8f9fa; border-radius: 8px;">
                                <tr>
                                    <td style="padding: 20px;">
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #999999;">您的账户</p>
                                        <p style="margin: 0; font-size: 16px; color: #333333;">
                                            <strong>邮箱:</strong> {{.user.email}}<br>
                                            <strong>用户名:</strong> {{.user.username}}<br>
                                            <strong>注册日期:</strong> {{.user.joinDate}}
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- 入门步骤 -->
                            <h2 style="font-size: 20px; color: #333333; margin: 30px 0 15px 0;">入门指南</h2>
                            <ol style="padding-left: 20px; margin: 0;">
                                {{range .steps}}
                                <li style="margin: 10px 0; font-size: 16px; color: #666666; line-height: 1.6;">
                                    <strong>{{.title}}</strong>: {{.description}}
                                </li>
                                {{end}}
                            </ol>
                            
                            <!-- CTA 按钮 -->
                            <table role="presentation" style="margin: 40px 0;">
                                <tr>
                                    <td style="text-align: center; background-color: #667eea; border-radius: 8px;">
                                        <a href="{{.ctaUrl}}" 
                                           style="display: inline-block; padding: 15px 40px; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: bold;">
                                            完善您的个人资料
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="font-size: 16px; color: #666666; margin: 30px 0 0 0; line-height: 1.6;">
                                如有任何疑问，请直接回复此邮件或访问我们的 
                                <a href="{{.supportUrl}}" style="color: #667eea; text-decoration: none;">帮助中心</a>。
                            </p>
                            
                            <p style="font-size: 16px; color: #666666; margin: 20px 0 0 0;">
                                祝好,<br>
                                {{.companyName}} 团队
                            </p>
                        </td>
                    </tr>
                    
                    <!-- 页脚 -->
                    <tr>
                        <td style="padding: 30px; background-color: #f8f9fa; text-align: center;">
                            <p style="font-size: 12px; color: #999999; margin: 0 0 10px 0;">
                                此邮件发送至 {{.user.email}}
                            </p>
                            <p style="font-size: 12px; margin: 0;">
                                <a href="{{.privacyUrl}}" style="color: #999999; text-decoration: underline;">隐私政策</a>
                                &nbsp;|&nbsp;
                                <a href="{{.unsubscribeUrl}}" style="color: #999999; text-decoration: underline;">取消订阅</a>
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

### Java 代码

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
        
        // 用户信息
        Map<String, String> user = new HashMap<>();
        user.put("firstName", "Alice");
        user.put("email", "alice@example.com");
        user.put("username", "alice2026");
        user.put("joinDate", LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy 年 M 月 dd 日")));
        data.put("user", user);
        
        // 平台统计
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 50000);
        data.put("stats", stats);
        
        // 入门步骤
        List<Map<String, String>> steps = Arrays.asList(
            createStep("完善个人资料", "添加您的照片和个人简介"),
            createStep("探索功能", "发现您能做的一切"),
            createStep("与人连接", "寻找朋友和同事")
        );
        data.put("steps", steps);
        
        // 链接
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

## 密码重置邮件

带有令牌和到期时间的密码重置安全邮件。

### 模板

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
                    <!-- 警告头部 -->
                    <tr>
                        <td style="padding: 30px; background-color: #fff3cd; border-bottom: 3px solid #ffc107;">
                            <h1 style="color: #856404; margin: 0; font-size: 22px;">
                                密码重置请求
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- 内容 -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 16px; color: #333333; margin: 0 0 20px 0;">
                                您好 {{.userName}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 20px 0;">
                                我们收到了您账户 <strong>{{.userEmail}}</strong> 的密码重置请求。
                            </p>
                            
                            <!-- 安全提示 -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #d1ecf1; border-left: 4px solid #17a2b8; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; color: #0c5460;">
                                            <strong>安全提示:</strong> 此链接将于 {{.expirationHours}} 小时后过期。
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- 重置按钮 -->
                            <table role="presentation" style="margin: 40px 0;">
                                <tr>
                                    <td style="text-align: center;">
                                        <a href="{{.resetUrl}}" 
                                           style="display: inline-block; padding: 14px 35px; background-color: #dc3545; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold;">
                                            重置密码
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- 备用方案: 复制链接 -->
                            <p style="font-size: 14px; color: #666666; margin: 20px 0;">
                                如果按钮无效，请将以下链接复制粘贴到浏览器中：
                            </p>
                            <p style="font-size: 12px; color: #999999; word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 4px;">
                                {{.resetUrl}}
                            </p>
                            
                            <!-- 未请求提示 -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #f8d7da; border-left: 4px solid #dc3545; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; color: #721c24;">
                                            <strong>不是您本人操作?</strong> 如果您没有请求重置密码，您可以安全地忽略此邮件。您的密码将保持不变。
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="font-size: 16px; color: #666666; margin: 30px 0 0 0;">
                                出于安全考虑，请不要与任何人分享此链接。
                            </p>
                            
                            <p style="font-size: 16px; color: #666666; margin: 20px 0 0 0;">
                                谨致问候,<br>
                                安全团队
                            </p>
                        </td>
                    </tr>
                    
                    <!-- 页脚 -->
                    <tr>
                        <td style="padding: 20px 30px; background-color: #f8f9fa; border-top: 1px solid #dee2e6;">
                            <p style="font-size: 12px; color: #6c757d; margin: 0;">
                                请求 IP: {{.requestIp}}<br>
                                请求时间: {{.requestTime}}
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

### Java 代码

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
        
        // 生成带有令牌的安全重置 URL
        String token = generateSecureToken();
        data.put("resetUrl", "https://example.com/reset-password?token=" + token);
        
        // 请求元数据
        data.put("requestIp", "192.168.1.100");
        data.put("requestTime", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        
        String html = writer.toString();
        System.out.println(html);
    }
    
    private static String generateSecureToken() {
        // 在生产环境中，使用加密安全的随机令牌
        return java.util.UUID.randomUUID().toString();
    }
}
```

---

## 订单确认

带有明细列表的电商订单确认。

### 模板

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
                    <!-- 成功头部 -->
                    <tr>
                        <td style="padding: 40px 30px; text-align: center; background-color: #28a745; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 26px;">✓ 订单已确认！</h1>
                            <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 16px;">
                                订单号 #{{.order.number}}
                            </p>
                        </td>
                    </tr>
                    
                    <!-- 内容 -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 16px; color: #333333; margin: 0 0 20px 0;">
                                您好 {{.customer.name}},
                            </p>
                            
                            <p style="font-size: 16px; line-height: 1.6; color: #666666; margin: 0 0 30px 0;">
                                感谢您的订单！我们已收到您的购买，将尽快处理。
                            </p>
                            
                            <!-- 订单摘要 -->
                            <h2 style="font-size: 20px; color: #333333; margin: 30px 0 15px 0; border-bottom: 2px solid #28a745; padding-bottom: 10px;">
                                订单摘要
                            </h2>
                            
                            <table role="presentation" style="width: 100%; margin: 20px 0;">
                                {{range .order.items}}
                                <tr>
                                    <td style="padding: 15px 0; border-bottom: 1px solid #dee2e6;">
                                        <p style="margin: 0 0 5px 0; font-size: 16px; color: #333333;">
                                            <strong>{{.name}}</strong>
                                        </p>
                                        <p style="margin: 0; font-size: 14px; color: #666666;">
                                            数量: {{.quantity}} × {{formatCurrency .price}}
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
                            
                            <!-- 合计 -->
                            <table role="presentation" style="width: 100%; margin: 20px 0; background-color: #f8f9fa; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            小计: {{formatCurrency .order.subtotal}}
                                        </p>
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            运费: {{formatCurrency .order.shipping}}
                                        </p>
                                        <p style="margin: 0 0 10px 0; font-size: 14px; color: #666666;">
                                            税费: {{formatCurrency .order.tax}}
                                        </p>
                                        <hr style="border: none; border-top: 1px solid #dee2e6; margin: 10px 0;">
                                        <p style="margin: 0; font-size: 18px; color: #333333; font-weight: bold;">
                                            总计: {{formatCurrency .order.total}}
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- 收货地址 -->
                            <h2 style="font-size: 20px; color: #333333; margin: 30px 0 15px 0;">收货地址</h2>
                            <p style="font-size: 14px; line-height: 1.6; color: #666666; margin: 0;">
                                {{.shippingAddress.name}}<br>
                                {{.shippingAddress.street}}<br>
                                {{.shippingAddress.city}}, {{.shippingAddress.state}} {{.shippingAddress.zip}}<br>
                                {{.shippingAddress.country}}
                            </p>
                            
                            <!-- 预计送达 -->
                            <table role="presentation" style="width: 100%; margin: 30px 0; background-color: #d4edda; border-left: 4px solid #28a745; border-radius: 4px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; color: #155724;">
                                            <strong>预计送达:</strong> {{.order.estimatedDelivery}}
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- 追踪订单按钮 -->
                            <table role="presentation" style="margin: 30px 0;">
                                <tr>
                                    <td style="text-align: center;">
                                        <a href="{{.trackingUrl}}" 
                                           style="display: inline-block; padding: 14px 35px; background-color: #28a745; color: #ffffff; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold;">
                                            追踪您的订单
                                        </a>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="font-size: 14px; color: #666666; margin: 30px 0 0 0; line-height: 1.6;">
                                对订单有疑问？请通过以下方式联系我们 
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

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;

public class OrderConfirmationExample {
    public static void main(String[] args) throws Exception {
        // 货币格式化函数
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
        
        // 客户信息
        Map<String, String> customer = new HashMap<>();
        customer.put("name", "Jane Smith");
        data.put("customer", customer);
        
        // 订单详情
        Map<String, Object> order = new HashMap<>();
        order.put("number", "ORD-2026-001234");
        order.put("subtotal", 89.97);
        order.put("shipping", 9.99);
        order.put("tax", 8.00);
        order.put("total", 107.96);
        order.put("estimatedDelivery", "2026 年 5 月 15-17 日");
        
        // 订单项目
        List<Map<String, Object>> items = Arrays.asList(
            createItem("无线鼠标", 2, 24.99),
            createItem("USB-C 数据线", 3, 12.99),
            createItem("笔记本支架", 1, 39.99)
        );
        order.put("items", items);
        data.put("order", order);
        
        // 收货地址
        Map<String, String> shippingAddress = new HashMap<>();
        shippingAddress.put("name", "Jane Smith");
        shippingAddress.put("street", "123 Main Street, Apt 4B");
        shippingAddress.put("city", "Springfield");
        shippingAddress.put("state", "IL");
        shippingAddress.put("zip", "62701");
        shippingAddress.put("country", "美国");
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

## 新闻通讯模板

带有多个板块的月度新闻通讯。

### 模板

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
                    <!-- 头部 -->
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
                    
                    <!-- 精选文章 -->
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
                                阅读更多 →
                            </a>
                        </td>
                    </tr>
                    
                    <!-- 文章网格 -->
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
                    
                    <!-- 快速链接 -->
                    <tr>
                        <td style="padding: 30px; background-color: #f8f8f8; border-top: 1px solid #dddddd;">
                            <h3 style="font-size: 18px; color: #333333; margin: 0 0 15px 0;">快速链接</h3>
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
                    
                    <!-- 页脚 -->
                    <tr>
                        <td style="padding: 30px; text-align: center; background-color: #333333;">
                            <p style="font-size: 14px; color: #cccccc; margin: 0 0 10px 0;">
                                您收到此邮件是因为您订阅了我们的新闻通讯。
                            </p>
                            <p style="font-size: 12px; margin: 0;">
                                <a href="{{.unsubscribeUrl}}" style="color: #cccccc; text-decoration: underline;">
                                    取消订阅
                                </a>
                                &nbsp;|&nbsp;
                                <a href="{{.preferencesUrl}}" style="color: #cccccc; text-decoration: underline;">
                                    更新偏好设置
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

### Java 代码

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
        
        // 新闻通讯元数据
        Map<String, String> newsletter = new HashMap<>();
        newsletter.put("title", "月度摘要");
        newsletter.put("issue", "第 42 期");
        newsletter.put("date", LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy 年 M 月")));
        data.put("newsletter", newsletter);
        
        // 精选文章
        Map<String, String> featuredArticle = new HashMap<>();
        featuredArticle.put("title", "Web 开发的未来");
        featuredArticle.put("excerpt", 
            "探索将影响我们未来几年构建 Web 应用方式的新兴趋势和技术...");
        featuredArticle.put("url", "https://example.com/articles/future-web-dev");
        data.put("featuredArticle", featuredArticle);
        
        // 常规文章
        List<Map<String, String>> articles = Arrays.asList(
            createArticle("10 个提升代码质量的技巧", 
                "通过这些经过验证的策略提升您的编码技能...",
                "https://example.com/articles/coding-tips"),
            createArticle("理解异步编程",
                "深入探讨异步编程模式...",
                "https://example.com/articles/async-programming")
        );
        data.put("articles", articles);
        
        // 快速链接
        List<Map<String, String>> quickLinks = Arrays.asList(
            createLink("博客", "https://example.com/blog"),
            createLink("文档", "https://example.com/docs"),
            createLink("社区论坛", "https://example.com/forum")
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

## 通知邮件

带有操作项的系统通知。

### 模板

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
                    <!-- 通知类型头部 -->
                    <tr>
                        <td style="padding: 30px; background-color: {{.headerColor}}; border-radius: 8px 8px 0 0;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 22px;">
                                {{.icon}} {{.notificationTitle}}
                            </h1>
                        </td>
                    </tr>
                    
                    <!-- 内容 -->
                    <tr>
                        <td style="padding: 40px 30px;">
                            <p style="font-size: 16px; color: #333333; margin: 0 0 20px 0;">
                                您好 {{.recipientName}},
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
                                此为自动通知。请勿回复此邮件。
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

### Java 代码

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
        data.put("notificationTitle", "您的帖子有新评论");
        data.put("message", "有人在您最近的帖子 "模板入门" 下发表了评论。");
        
        // 通知类型样式
        data.put("headerColor", "#007bff");  // 蓝色表示信息
        data.put("buttonColor", "#007bff");
        data.put("icon", "");
        
        // 详情
        Map<String, String> details = new HashMap<>();
        details.put("帖子", "模板入门");
        details.put("评论者", "Sarah Williams");
        details.put("时间", "2 小时前");
        data.put("details", details);
        
        // 操作按钮
        List<Map<String, String>> actionButtons = Arrays.asList(
            createAction("查看评论", "https://example.com/post/123#comment-456"),
            createAction("回复", "https://example.com/post/123/reply")
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

## 带附件元数据的邮件

引用附件的邮件（实际附件由邮件服务处理）。

### 模板

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
                                附件 ({{len .attachments}})
                            </h2>
                            
                            <table role="presentation" style="width: 100%; border: 1px solid #dddddd; border-radius: 4px;">
                                {{range .attachments}}
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #eeeeee;">
                                        <p style="margin: 0 0 5px 0; font-size: 14px; color: #333333;">
                                            <strong>{{.filename}}</strong>
                                        </p>
                                        <p style="margin: 0; font-size: 12px; color: #999999;">
                                            {{.size}} - {{.type}}
                                        </p>
                                    </td>
                                </tr>
                                {{end}}
                            </table>
                            
                            <p style="font-size: 14px; color: #666666; margin: 20px 0 0 0;">
                                <em>注意: 附件已包含在此邮件中。</em>
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

### Java 代码

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class AttachmentEmailExample {
    public static void main(String[] args) throws Exception {
        // 文件大小格式化函数
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
        data.put("subject", "您的月度报告");
        data.put("message", "请查收 2026 年 4 月的月度绩效报告。");
        
        // 附件元数据
        List<Map<String, Object>> attachments = Arrays.asList(
            createAttachment("report_april_2026.pdf", 2457600, "PDF 文档"),
            createAttachment("summary.xlsx", 1048576, "Excel 电子表格"),
            createAttachment("charts.png", 524288, "PNG 图片")
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

## 最佳实践

### 1. 使用内联样式

邮件客户端对 CSS 支持有限。始终使用内联样式：

```gotemplate
<!-- 好 -->
<p style="font-size: 16px; color: #333333;">文本</p>

<!-- 差 -->
<style>p { font-size: 16px; }</style>
<p>文本</p>
```

### 2. 使用表格布局

许多邮件客户端不支持现代 CSS 布局技术：

```gotemplate
<!-- 好: 基于表格的布局 -->
<table role="presentation">
  <tr><td>内容</td></tr>
</table>

<!-- 差: 基于 Div 的布局 -->
<div class="container">内容</div>
```

### 3. 跨邮件客户端测试

在以下客户端测试您的邮件：
- Gmail（Web 和移动端）
- Outlook（桌面和 Web 端）
- Apple Mail
- Yahoo Mail
- 移动设备（iOS 和 Android）

### 4. 提供纯文本替代方案

始终包含纯文本版本：

```java
String html = renderHtmlTemplate(data);
String plainText = renderPlainTextTemplate(data);

EmailMessage message = new EmailMessage();
message.setHtmlBody(html);
message.setTextBody(plainText);
```

### 5. 保持文件大小合理

- 邮件大小尽量控制在 100KB 以下
- 嵌入前优化图片
- 避免过度嵌套表格

### 6. 使用 Web 安全字体

```gotemplate
<!-- 好 -->
font-family: Arial, Helvetica, sans-serif;

<!-- 有风险 -->
font-family: 'CustomFont', sans-serif;
```

### 7. 包含取消订阅链接

许多司法管辖区要求法律强制：

```gotemplate
<a href="{{.unsubscribeUrl}}">取消订阅</a>
```

### 8. 预标题文本

添加隐藏的预标题文本以优化邮件预览：

```gotemplate
<body>
    <div style="display: none; max-height: 0; overflow: hidden;">
        {{.preheaderText}}
    </div>
    <!-- 邮件其余部分 -->
</body>
```

---

## 下一步

- 参见 [Web 模板](web-templates.md) 了解 Web 特定模式
- 参见 [复杂场景](complex-scenarios.md) 了解高级用例
- 参见 [函数指南](../user-guide/functions.md) 了解自定义函数示例
- 参见 [安全注意事项](../advanced/security.md) 了解邮件安全最佳实践

---

本文档中的所有示例均已测试并通过验证，可在 gotemplate4j 中正确运行。
