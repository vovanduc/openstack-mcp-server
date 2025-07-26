# Hướng dẫn tạo VM OpenStack với Mạng nội bộ, Floating IP và SSH Key

Hướng dẫn này mô tả chi tiết cách tạo một máy ảo (VM) trong OpenStack, đặt nó trên một mạng nội bộ, cấu hình router để kết nối ra Internet, gán Floating IP và sử dụng SSH Key Pair để truy cập. Đây là mô hình mạng được khuyến nghị cho môi trường production.

## 1. Tổng quan kiến trúc

*   **Mạng nội bộ (Internal Network):** Nơi VM của bạn sẽ nhận IP private (Fixed IP).
*   **Router:** Cầu nối giữa mạng nội bộ và mạng public (External Network).
*   **Floating IP:** Địa chỉ IP public được gán cho VM để truy cập từ Internet.
*   **SSH Key Pair:** Phương pháp an toàn và được khuyến nghị để truy cập VM.

## 2. Chuẩn bị

Đảm bảo bạn đã cài đặt và cấu hình OpenStack CLI. Các lệnh dưới đây sử dụng thông tin xác thực từ file `openstack.env` của bạn.

**Thông tin xác thực OpenStack của bạn (từ `openstack.env`):**

*   `OS_AUTH_TYPE=v3applicationcredential`
*   `OS_AUTH_URL=http://10.10.184.11:5000`
*   `OS_IDENTITY_API_VERSION=3`
*   `OS_REGION_NAME=HoChiMinh`
*   `OS_INTERFACE=public`
*   `OS_APPLICATION_CREDENTIAL_ID=63dd89ed36174146a0d0e800f07e7850`
*   `OS_APPLICATION_CREDENTIAL_SECRET='GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg'`

**Các ID quan trọng đã xác định:**

*   **External Network ID:** `f7b68883-7aa8-4e78-93b6-ab8f684c501b` (tên: `external-net`)
*   **Default Security Group ID:** `f1cc6373-7f05-40c1-9921-0f7c3f815d9b` (tên: `default`)
*   **Image ID:** `d790dfea-e532-4f94-9189-3606ff124ae5` (tên: `Ubuntu2404`)
*   **Flavor ID:** `dcnetcloud.package.2.8`

## 3. Thiết lập Mạng

### 3.1. Tạo Mạng nội bộ (Internal Network) và Subnet

Tạo một mạng riêng cho các VM của bạn.

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' network create my-internal-net
# Output: ID của mạng nội bộ (ví dụ: 12bf2253-6be6-4408-ac93-d2713da3d813)
```

Tạo một subnet cho mạng nội bộ này.

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' subnet create --network my-internal-net --subnet-range 192.168.100.0/24 --gateway 192.168.100.1 my-internal-subnet
# Output: ID của subnet nội bộ (ví dụ: d6fba35e-c92e-4db8-b4c3-b87aac73b006)
```

### 3.2. Tạo Router và kết nối mạng

Router sẽ là cầu nối giữa mạng nội bộ và mạng public.

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' router create my-router
# Output: ID của router (ví dụ: 748dd669-1acc-48fa-a670-22e7e9824a95)
```

Gắn subnet nội bộ vào router.

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' router add subnet my-router my-internal-subnet
```

Đặt cổng ngoài (external gateway) cho router, kết nối nó với mạng public (`external-net`).

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' router set my-router --external-gateway f7b68883-7aa8-4e78-93b6-ab8f684c501b
```

## 4. Thiết lập SSH Key Pair

SSH Key Pair là cách an toàn nhất để truy cập VM.

### 4.1. Tạo SSH Key Pair trên máy tính của bạn

Mở terminal trên máy tính của bạn và chạy lệnh sau. Bạn có thể được hỏi về passphrase, nhấn Enter để bỏ qua nếu không muốn đặt.

```bash
sah-keygen -t rsa -b 2048 -f my_openstack_key -N ''
```
Lệnh này sẽ tạo hai file: `my_openstack_key` (khóa riêng tư) và `my_openstack_key.pub` (khóa công khai).

**Quan trọng:** Đặt quyền cho khóa riêng tư để chỉ bạn có thể đọc nó.

```bash
chmod 400 my_openstack_key
```

### 4.2. Upload Khóa Công khai lên OpenStack

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' keypair create --public-key my_openstack_key.pub my-openstack-key
# Output: Xác nhận khóa đã được tạo
```

## 5. Tạo VM

Bây giờ, chúng ta sẽ tạo VM trên mạng nội bộ và gắn SSH Key Pair.

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' server create \
  --image d790dfea-e532-4f94-9189-3606ff124ae5 \
  --flavor dcnetcloud.package.2.8 \
  --network my-internal-net \
  --security-group f1cc6373-7f05-40c1-9921-0f7c3f815d9b \
  --boot-from-volume 70 \
  --key-name my-openstack-key \
  debug-vm-internal-with-key
# Output: Thông tin VM đang được tạo. Ghi lại ID của VM.
```

Chờ cho VM chuyển sang trạng thái `ACTIVE`. Bạn có thể kiểm tra trạng thái bằng lệnh:

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' server show debug-vm-internal-with-key
# Tìm trường 'addresses' để lấy Fixed IP (ví dụ: my-internal-net=192.168.100.226)
```

## 6. Gán Floating IP

### 6.1. Cấp phát Floating IP

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' floating ip create f7b68883-7aa8-4e78-93b6-ab8f684c501b
# Output: Địa chỉ Floating IP (ví dụ: 103.245.245.208)
```

### 6.2. Gán Floating IP cho VM

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' server add floating ip debug-vm-internal-with-key 103.245.245.208
# Thay 103.245.245.208 bằng Floating IP bạn vừa cấp phát.
```

## 7. Truy cập VM

Sau khi VM đã ACTIVE và Floating IP đã được gán, bạn có thể SSH vào VM bằng khóa riêng tư của mình.

```bash
ssh -i my_openstack_key ubuntu@<Floating_IP_của_VM>
# Ví dụ: ssh -i my_openstack_key ubuntu@103.245.245.208
```
Tên người dùng mặc định cho Ubuntu là `ubuntu`.

## 8. Thêm Volume 50GB (Tùy chọn)

Để thêm một volume 50GB, bạn cần tạo volume trước, sau đó gắn nó vào VM.

### 8.1. Tạo Volume

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' volume create --size 50 my-data-volume
# Output: ID của volume (ví dụ: 1a2b3c4d-...)
```

### 8.2. Gắn Volume vào VM

```bash
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' server add volume debug-vm-internal-with-key my-data-volume
```

Sau khi gắn, bạn cần SSH vào VM để định dạng và mount volume.

## 9. Dọn dẹp (Tùy chọn)

Để xóa các tài nguyên đã tạo:

```bash
# Xóa VM
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' server delete debug-vm-internal-with-key

# Xóa Floating IP (nếu không còn được sử dụng)
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' floating ip delete 103.245.245.208

# Xóa Volume (nếu có)
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' volume delete my-data-volume

# Gỡ subnet khỏi router
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' router remove subnet my-router my-internal-subnet

# Xóa router
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' router delete my-router

# Xóa subnet nội bộ
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' subnet delete my-internal-subnet

# Xóa mạng nội bộ
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' network delete my-internal-net

# Xóa keypair
openstack --os-auth-type v3applicationcredential --os-auth-url http://10.10.184.11:5000 --os-identity-api-version 3 --os-region-name HoChiMinh --os-interface public --os-application-credential-id 63dd89ed36174146a0d0e800f07e7850 --os-application-credential-secret 'GO5rEbbA-werunzzEmd8riVFSmsgHNMT8QjKh4MRYEhkGncAY06Y4iUj5uPzUU2BLqmF8q8uGdjXThdH5Hmnvg' keypair delete my-openstack-key

