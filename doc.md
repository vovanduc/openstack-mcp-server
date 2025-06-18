# OpenStack MCP Server - Tài liệu chi tiết

## 🎯 Tổng quan dự án

**OpenStack MCP Server** là một ứng dụng Java cho phép bạn sử dụng AI Assistant (như Claude Desktop) để quản lý tài nguyên OpenStack một cách tự nhiên và an toàn. Thay vì phải nhớ và gõ các lệnh OpenStack phức tạp, bạn chỉ cần nói chuyện với AI và nó sẽ thực hiện các tác vụ quản lý cloud cho bạn.

### 🌟 Tại sao cần dự án này?

- **Đơn giản hóa**: Không cần nhớ các lệnh OpenStack phức tạp
- **Tự động hóa**: AI có thể thực hiện nhiều tác vụ quản lý tự động
- **An toàn**: Sử dụng Application Credentials thay vì password thông thường
- **Thân thiện**: Giao tiếp tự nhiên bằng tiếng Việt hoặc tiếng Anh với AI

## 🏗️ Kiến trúc dự án

### Công nghệ sử dụng

| Thành phần | Phiên bản | Mục đích |
|------------|-----------|----------|
| **Java** | 21 | Ngôn ngữ lập trình chính |
| **Spring Boot** | 3.4.4 | Framework tạo ứng dụng web |
| **Spring AI** | 1.0.0-M7 | Hỗ trợ giao tiếp với AI qua MCP |
| **Maven** | 3.8+ | Quản lý thư viện và build |
| **OpenStack CLI** | - | Thực thi lệnh OpenStack |

### Cấu trúc thư mục

```
openstack-mcp-server/
├── src/main/java/ro/dragomiralin/openstack_mcp_server/
│   ├── OpenstackMcpServerApplication.java    # Khởi động ứng dụng
│   └── service/
│       └── OpenStackCommander.java          # Xử lý lệnh OpenStack
├── src/main/resources/
│   └── application.yml                      # Cấu hình ứng dụng
├── pom.xml                                  # Quản lý dependencies
└── README.md                               # Hướng dẫn sử dụng
```

## 🔧 Các thành phần chính

### 1. OpenstackMcpServerApplication.java
**Vai trò**: Điểm khởi đầu của ứng dụng

```java
@SpringBootApplication
public class OpenstackMcpServerApplication {
    // Khởi động ứng dụng Spring Boot
    // Cấu hình các tool cho AI sử dụng
}
```

**Chức năng**:
- Khởi động ứng dụng Spring Boot
- Đăng ký OpenStackCommander như một tool cho AI
- Cấu hình MCP server

### 2. OpenStackCommander.java
**Vai trò**: "Người chỉ huy" thực thi các lệnh OpenStack

**Các phương thức chính**:

#### `runOpenStackCommand(String command)`
- **Mục đích**: Thực thi lệnh OpenStack
- **Ví dụ**: `runOpenStackCommand("openstack server list")`
- **Kết quả**: Danh sách các server trong OpenStack

#### `execute(String command)`
- **Mục đích**: Xử lý việc thực thi lệnh với timeout
- **Tính năng**:
  - Timeout 60 giây cho mỗi lệnh
  - Xử lý lỗi an toàn
  - Cấu hình môi trường OpenStack

#### `isOpenStackInstalled()`
- **Mục đích**: Kiểm tra OpenStack CLI đã được cài đặt chưa
- **Kết quả**: `true` nếu đã cài, `false` nếu chưa

#### `getOpenStackDiagnostics()`
- **Mục đích**: Chẩn đoán khi có lỗi
- **Thông tin cung cấp**:
  - Vị trí file OpenStack CLI
  - Phiên bản OpenStack
  - Trạng thái cấu hình môi trường

## ⚙️ Cấu hình chi tiết

### File application.yml

```yaml
spring:
  main:
    web-application-type: none    # Không phải web app thông thường
    banner-mode: off             # Tắt banner Spring Boot
  ai:
    mcp:
      server:
        name: openstack-mcp-server
        version: 0.0.1

# Cấu hình OpenStack
openstack:
  authType: v3applicationcredential    # Loại xác thực
  authUrl: "https://your-openstack.com" # URL xác thực
  identityApiVersion: "3"              # Phiên bản API
  regionName: "RegionOne"              # Tên region
  interface: public                    # Interface sử dụng
  applicationCredentialId: "your-id"   # ID credential
  applicationCredentialSecret: "your-secret" # Secret credential

server:
  port: 8080                          # Port chạy ứng dụng
```

### Giải thích các thông số OpenStack

| Thông số | Ý nghĩa | Ví dụ |
|----------|---------|-------|
| `authType` | Loại xác thực | `v3applicationcredential` |
| `authUrl` | URL để xác thực | `https://keystone.example.com` |
| `regionName` | Tên region OpenStack | `RegionOne`, `us-east-1` |
| `applicationCredentialId` | ID của application credential | `abc123def456` |
| `applicationCredentialSecret` | Secret của credential | `your-secret-key` |

## 🚀 Cách sử dụng

### Bước 1: Chuẩn bị môi trường
```bash
# Kiểm tra Java 21
java -version

# Kiểm tra Maven
mvn -version

# Kiểm tra OpenStack CLI
openstack --version
```

### Bước 2: Cấu hình OpenStack
1. Mở file `src/main/resources/application.yml`
2. Cập nhật thông tin OpenStack của bạn:
   ```yaml
   openstack:
     authUrl: "https://your-openstack.com"
     regionName: "your-region"
     applicationCredentialId: "your-credential-id"
     applicationCredentialSecret: "your-credential-secret"
   ```

### Bước 3: Build ứng dụng
```bash
mvn clean package
```

### Bước 4: Tích hợp với Claude Desktop
Tạo file `claude-desktop.json`:
```json
{
  "mcpServers": {
    "openstack-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/openstack-mcp-server-0.0.1.jar",
        "--port",
        "8080",
        "--host",
        "localhost"
      ]
    }
  }
}
```

### Bước 5: Sử dụng với AI
Sau khi tích hợp, bạn có thể nói chuyện với Claude Desktop:

**Ví dụ các câu lệnh**:
- "Liệt kê tất cả server của tôi"
- "Tạo một server mới với flavor m1.small"
- "Xóa server có tên test-server"
- "Hiển thị thông tin về project hiện tại"

## 📋 Các lệnh OpenStack phổ biến

### Quản lý Server
```bash
# Liệt kê server
openstack server list

# Tạo server mới
openstack server create --image ubuntu-20.04 --flavor m1.small my-server

# Xóa server
openstack server delete my-server

# Khởi động/dừng server
openstack server start my-server
openstack server stop my-server
```

### Quản lý Image
```bash
# Liệt kê images
openstack image list

# Tạo image mới
openstack image create --disk-format qcow2 my-image

# Xóa image
openstack image delete my-image
```

### Quản lý Network
```bash
# Liệt kê networks
openstack network list

# Tạo network mới
openstack network create my-network

# Tạo subnet
openstack subnet create --network my-network --subnet-range 192.168.1.0/24 my-subnet
```

## 🔍 Xử lý lỗi thường gặp

### 1. Lỗi "OpenStack CLI not found"
**Nguyên nhân**: OpenStack CLI chưa được cài đặt
**Giải pháp**:
```bash
# Ubuntu/Debian
sudo apt install python3-openstackclient

# CentOS/RHEL
sudo yum install python3-openstackclient

# macOS
pip3 install python-openstackclient
```

### 2. Lỗi "Authentication failed"
**Nguyên nhân**: Thông tin xác thực sai
**Giải pháp**:
- Kiểm tra lại `applicationCredentialId` và `applicationCredentialSecret`
- Đảm bảo `authUrl` đúng
- Kiểm tra quyền truy cập của application credential

### 3. Lỗi "Region not found"
**Nguyên nhân**: Tên region không đúng
**Giải pháp**:
```bash
# Liệt kê các region có sẵn
openstack region list
```

### 4. Lỗi "Command timeout"
**Nguyên nhân**: Lệnh chạy quá lâu
**Giải pháp**:
- Kiểm tra kết nối mạng
- Thử lại lệnh
- Tăng timeout trong code nếu cần

## 🛡️ Bảo mật

### Application Credentials
- **Ưu điểm**: An toàn hơn password, có thể thu hồi dễ dàng
- **Cách tạo**:
  1. Đăng nhập OpenStack Dashboard
  2. Vào Identity > Application Credentials
  3. Tạo credential mới với quyền phù hợp

### Best Practices
- Không commit secret vào git
- Sử dụng biến môi trường cho production
- Giới hạn quyền của application credential
- Thay đổi credential định kỳ

## 🔮 Phát triển tương lai

### Tính năng có thể thêm
- [ ] Hỗ trợ nhiều OpenStack environments
- [ ] Cache kết quả để tăng tốc độ
- [ ] Webhook notifications
- [ ] Backup automation
- [ ] Cost optimization suggestions
- [ ] Multi-language support

### Cải tiến kỹ thuật
- [ ] Async command execution
- [ ] Better error handling
- [ ] Logging improvements
- [ ] Unit tests coverage
- [ ] Docker containerization

## 📞 Hỗ trợ

Nếu gặp vấn đề, bạn có thể:
1. Kiểm tra logs của ứng dụng
2. Sử dụng `getOpenStackDiagnostics()` để chẩn đoán
3. Kiểm tra cấu hình OpenStack
4. Tham khảo tài liệu OpenStack chính thức

---

**Lưu ý**: Dự án này được thiết kế để làm việc với Claude Desktop và các AI assistant khác hỗ trợ MCP. Đảm bảo bạn đã cài đặt và cấu hình đúng các thành phần cần thiết trước khi sử dụng. 