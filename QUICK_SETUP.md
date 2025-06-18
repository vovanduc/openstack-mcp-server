# Quick Setup OpenStack MCP Server

## Bước 1: Clone và cài đặt dependencies
```bash
git clone <your-repo-url>
cd openstack-mcp-server

# Cài đặt Java 21
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Cài đặt Maven
brew install maven

# Cài đặt OpenStack CLI
brew install pipx
pipx install python-openstackclient
```

## Bước 2: Cấu hình OpenStack
```bash
# Copy template và chỉnh sửa
cp openstack.env.template openstack.env
nano openstack.env
```

## Bước 3: Build và test
```bash
# Build dự án
mvn clean package

# Test thủ công
./start-mcp.sh
```

## Bước 4: Sử dụng với Cursor
1. Mở dự án trong Cursor
2. File `.cursor/mcp.json` đã được cấu hình sẵn
3. Mở chat với AI (Cmd + L)
4. Test: "Liệt kê server OpenStack của tôi"

## Troubleshooting
- **Lỗi Java**: `java -version` để kiểm tra
- **Lỗi OpenStack**: `openstack --version` để kiểm tra
- **Lỗi MCP**: Kiểm tra file `openstack.env` có đúng không 