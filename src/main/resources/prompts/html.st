
你将把一段 Markdown 转换为适合电子邮件正文的简约 HTML。请严格遵循以下要求，只返回最终 HTML 字符串（不要包含说明文字或代码块围栏）。

输出要求

使用 <html><body>...</body></html> 完整结构。

仅使用邮件友好标签：h1–h4, p, a, ul, ol, li, blockquote, code, pre, img, table, thead, tbody, tr, th, td, hr, strong, em, br, span, div。

只用内联样式（不使用 <style>、类名或外链 CSS），风格极简、留白适中、良好可读性。

整体容器宽度不超过 600px，居中显示，左右内边距适度。

字体：系统字体栈（如 -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif），行高约 1.6。

颜色：正文深灰（如 #333），链接蓝（如 #1a73e8），浅灰分隔线（如 #e5e7eb）。

图片 img：必须包含 alt，最大宽度 100%，自适应。

表格：边框合并、单元格内边距、斑马纹可选但需简洁。

代码：行内 code 使用浅灰底和圆角；多行 pre 可横向滚动。

引用 blockquote 左侧细竖线、浅灰底。

不注入脚本，不使用表单、视频、音频、iframe。

只返回 HTML，不返回其他文字。

基础样式（内联到相应标签）

外层容器 <div>：
max-width:600px;margin:0 auto;padding:24px 20px;color:#333;font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;line-height:1.6;

标题 h1–h4：上大下小间距；h1 字号≈24px，h2≈20px。

段落 p：margin:12px 0;

链接 a：color:#1a73e8;text-decoration:none;（如需下划线可加 text-decoration:underline;）。

分隔线 hr：border:none;border-top:1px solid #e5e7eb;margin:20px 0;

引用 blockquote：margin:12px 0;padding:8px 12px;border-left:3px solid #e5e7eb;background:#f8fafc;

行内代码 code：background:#f3f4f6;padding:2px 6px;border-radius:4px;font-family:ui-monospace,SFMono-Regular,Menlo,Monaco,Consolas,monospace;

代码块 pre：background:#f3f4f6;padding:12px;border-radius:6px;overflow:auto;

表格 table：width:100%;border-collapse:collapse;margin:12px 0;

表头/单元格 th,td：border:1px solid #e5e7eb;padding:8px;text-align:left;

图片 img：max-width:100%;height:auto;border:none;

转换规则

Markdown 标题（# ~ ####）转成对应 h1–h4。

普通段落转为 <p>；换行保留为 <br> 或拆分段落。

列表 -/*/+ 与有序列表 1. 转为 ul/ol > li。

链接 [text](url) 转为 <a>，保留原始 URL。

图片 ![alt](url) 转为 <img alt="alt" src="url">。

代码：行内 `code`→<code>；代码块 → <pre><code>...</code></pre>，不要语法高亮类名。

表格按邮件安全方式渲染为 table/thead/tbody。

保留基本强调：**bold**→<strong>，*italic*→<em>。

若输入含不安全/不支持元素，忽略或转义为纯文本。

输入

将下面的 Markdown 转换为符合上述规范的 HTML：

{markdown_input}