# OpenStack API Flow - Lấy thông tin Image gốc từ Volume

## 📋 Tổng quan

Khi server có trạng thái image là `N/A (booted from volume)`, điều này có nghĩa là:
- Server được boot trực tiếp từ volume (không phải từ image)
- Volume đã chứa OS hoàn chỉnh và có thể boot được
- Không có image ID trực tiếp trong thông tin server
- Thông tin image gốc được lưu trong metadata của volume

## 🔄 Flow hoàn chỉnh

### 1. Lấy danh sách servers
```bash
openstack server list --format table
```

### 2. Lấy thông tin chi tiết server
```bash
openstack server show <server_name>
```

### 3. Lấy volume ID từ server
Từ field `volumes_attached` trong thông tin server:
```
volumes_attached                    | delete_on_termination='True',
                                   | id='3b57d708-230b-4ffc-9f74-f6b5bbdc6140'
```

### 4. Lấy thông tin volume
```bash
openstack volume show <volume_id>
```

### 5. Lấy image ID từ volume metadata
Từ field `volume_image_metadata` trong thông tin volume:
```
volume_image_metadata          | {'signature_verified': 'False', 
                                | 'owner_specified.openstack.md5': '', 
                                | 'owner_specified.openstack.sha256': '', 
                                | 'owner_specified.openstack.object': 'images/Ubuntu2404', 
                                | 'os_distro': 'ubuntu', 
                                | 'os_version': '24.04', 
                                | 'os_admin_user': 'root', 
                                | 'description': 'Ubuntu 24.04', 
                                | 'hw_qemu_guest_agent': 'yes', 
                                | 'image_id': 'd790dfea-e532-4f94-9189-3606ff124ae5', 
                                | 'image_name': 'Ubuntu2404', 
                                | 'checksum': '4a855177dae17e0716c258665576c40e', 
                                | 'container_format': 'bare', 
                                | 'disk_format': 'qcow2', 
                                | 'min_disk': '10', 
                                | 'min_ram': '2048', 
                                | 'size': '612835328'}
```

### 6. Lấy thông tin image gốc
```bash
openstack image show <image_id>
```

## 💻 PHP OpenStack SDK Implementation

### Cài đặt SDK
```bash
composer require php-opencloud/openstack
```

### Khởi tạo OpenStack Client
```php
<?php

use OpenStack\OpenStack;

$openstack = new OpenStack([
    'authUrl' => 'http://control.cercatrova.uk:5000',
    'region'  => 'HoChiMinh',
    'user'    => [
        'id'       => '3ca682bb63c74abe9fbd8ab9f8b8510a',
        'password' => 'UwO-wCvn3rwUy7dT7EeMa_Fyz-reYgMjVM39PG5AQwcWgdebuVD3vbNk3hmo76yDhVkWm71Vs7xI97udmLqx6Q'
    ],
    'scope'   => [
        'project' => [
            'id' => '29c03b921dd540b4a6ef5a88db34b70e'
        ]
    ]
]);
```

### Lấy danh sách servers
```php
// Lấy service Compute
$compute = $openstack->computeV2(['region' => 'HoChiMinh']);

// Lấy danh sách servers
$servers = $compute->listServers();

foreach ($servers as $server) {
    echo "Server: " . $server->name . " (ID: " . $server->id . ")\n";
    echo "Status: " . $server->status . "\n";
    echo "Image: " . $server->imageId . "\n";
    echo "---\n";
}
```

### Lấy thông tin chi tiết server
```php
// Lấy thông tin chi tiết server cụ thể
$serverName = 'duc-test';
$server = $compute->getServer(['id' => $serverName]);

echo "Server Details:\n";
echo "Name: " . $server->name . "\n";
echo "ID: " . $server->id . "\n";
echo "Status: " . $server->status . "\n";
echo "Image ID: " . $server->imageId . "\n"; // Sẽ là null nếu boot từ volume

// Lấy thông tin volumes attached
$volumes = $server->listVolumes();
foreach ($volumes as $volume) {
    echo "Volume ID: " . $volume->id . "\n";
    echo "Volume Size: " . $volume->size . " GB\n";
    echo "Volume Status: " . $volume->status . "\n";
}
```

### Lấy thông tin volume và image gốc
```php
// Lấy service Block Storage
$blockStorage = $openstack->blockStorageV3(['region' => 'HoChiMinh']);

// Lấy thông tin volume
$volumeId = '3b57d708-230b-4ffc-9f74-f6b5bbdc6140';
$volume = $blockStorage->getVolume(['id' => $volumeId]);

echo "Volume Details:\n";
echo "ID: " . $volume->id . "\n";
echo "Size: " . $volume->size . " GB\n";
echo "Status: " . $volume->status . "\n";

// Lấy metadata của volume
$metadata = $volume->metadata;
echo "Volume Metadata:\n";
print_r($metadata);

// Lấy image ID từ volume metadata
if (isset($metadata['image_id'])) {
    $originalImageId = $metadata['image_id'];
    echo "Original Image ID: " . $originalImageId . "\n";
}
```

### Lấy thông tin image gốc
```php
// Lấy service Image
$imageService = $openstack->imageV2(['region' => 'HoChiMinh']);

// Lấy thông tin image gốc
$imageId = 'd790dfea-e532-4f94-9189-3606ff124ae5';
$image = $imageService->getImage(['id' => $imageId]);

echo "Original Image Details:\n";
echo "ID: " . $image->id . "\n";
echo "Name: " . $image->name . "\n";
echo "Status: " . $image->status . "\n";
echo "Size: " . $image->size . " bytes\n";
echo "Disk Format: " . $image->diskFormat . "\n";
echo "Container Format: " . $image->containerFormat . "\n";

// Lấy properties của image
$properties = $image->properties;
echo "Image Properties:\n";
echo "OS: " . ($properties['os_distro'] ?? 'N/A') . "\n";
echo "OS Version: " . ($properties['os_version'] ?? 'N/A') . "\n";
echo "Admin User: " . ($properties['os_admin_user'] ?? 'N/A') . "\n";
echo "Description: " . ($properties['description'] ?? 'N/A') . "\n";
```

### Function hoàn chỉnh
```php
<?php

function getOriginalImageFromServer($openstack, $serverName) {
    try {
        $compute = $openstack->computeV2(['region' => 'HoChiMinh']);
        $blockStorage = $openstack->blockStorageV3(['region' => 'HoChiMinh']);
        $imageService = $openstack->imageV2(['region' => 'HoChiMinh']);
        
        // 1. Lấy thông tin server
        $server = $compute->getServer(['id' => $serverName]);
        
        echo "Server: {$server->name} (ID: {$server->id})\n";
        echo "Status: {$server->status}\n";
        echo "Image ID: " . ($server->imageId ?? 'N/A (booted from volume)') . "\n";
        
        // 2. Lấy volumes attached
        $volumes = $server->listVolumes();
        
        foreach ($volumes as $volume) {
            echo "\nVolume ID: {$volume->id}\n";
            
            // 3. Lấy thông tin chi tiết volume
            $volumeDetails = $blockStorage->getVolume(['id' => $volume->id]);
            $metadata = $volumeDetails->metadata;
            
            // 4. Kiểm tra có image_id trong metadata không
            if (isset($metadata['image_id'])) {
                $originalImageId = $metadata['image_id'];
                echo "Original Image ID: {$originalImageId}\n";
                
                // 5. Lấy thông tin image gốc
                $image = $imageService->getImage(['id' => $originalImageId]);
                $properties = $image->properties;
                
                echo "Original Image Details:\n";
                echo "- Name: {$image->name}\n";
                echo "- OS: " . ($properties['os_distro'] ?? 'N/A') . "\n";
                echo "- OS Version: " . ($properties['os_version'] ?? 'N/A') . "\n";
                echo "- Admin User: " . ($properties['os_admin_user'] ?? 'N/A') . "\n";
                echo "- Size: " . number_format($image->size) . " bytes\n";
                
                return $image;
            } else {
                echo "No original image found in volume metadata\n";
            }
        }
        
    } catch (Exception $e) {
        echo "Error: " . $e->getMessage() . "\n";
        return null;
    }
}

// Sử dụng function
$originalImage = getOriginalImageFromServer($openstack, 'duc-test');
```

## 📊 Cấu trúc Response JSON

```json
{
    "server": {
        "id": "e70b8085-fa01-459f-978d-14c69a332eba",
        "name": "duc-test",
        "status": "ACTIVE",
        "image_id": null,
        "volumes": [
            {
                "id": "3b57d708-230b-4ffc-9f74-f6b5bbdc6140",
                "size": 50,
                "status": "in-use"
            }
        ]
    },
    "original_image": {
        "id": "d790dfea-e532-4f94-9189-3606ff124ae5",
        "name": "Ubuntu2404",
        "os_distro": "ubuntu",
        "os_version": "24.04",
        "os_admin_user": "root",
        "size": 612835328,
        "disk_format": "qcow2",
        "container_format": "bare",
        "min_disk": 10,
        "min_ram": 2048,
        "visibility": "public",
        "status": "active"
    }
}
```

## 🛡️ Error Handling

```php
try {
    $originalImage = getOriginalImageFromServer($openstack, 'duc-test');
    if ($originalImage) {
        echo "Successfully retrieved original image\n";
    } else {
        echo "No original image found\n";
    }
} catch (OpenStack\Common\Error\BadResponseError $e) {
    echo "API Error: " . $e->getMessage() . "\n";
} catch (Exception $e) {
    echo "General Error: " . $e->getMessage() . "\n";
}
```

## 🔍 Lưu ý quan trọng

### Khi nào có "N/A (booted from volume)":
1. **Server được tạo từ volume có sẵn** (không phải từ image)
2. **Volume đã chứa OS hoàn chỉnh** và có thể boot được
3. **Không có image ID trực tiếp** trong thông tin server

### Lợi ích của boot từ volume:
- ✅ **Persistent storage** - dữ liệu không mất khi VM bị xóa
- ✅ **Snapshot support** - có thể backup dễ dàng
- ✅ **Volume management** - quản lý storage độc lập
- ✅ **Migration support** - di chuyển VM giữa các host

### Các trường hợp đặc biệt:
1. **Volume không có metadata**: Có thể là volume được tạo thủ công
2. **Metadata không có image_id**: Volume có thể được clone từ volume khác
3. **Image đã bị xóa**: Image gốc có thể đã bị xóa nhưng volume vẫn còn

## 📝 Ví dụ thực tế

### Server: duc-test
- **Server ID**: e70b8085-fa01-459f-978d-14c69a332eba
- **Status**: ACTIVE
- **Image**: N/A (booted from volume)
- **Volume ID**: 3b57d708-230b-4ffc-9f74-f6b5bbdc6140
- **Original Image ID**: d790dfea-e532-4f94-9189-3606ff124ae5
- **Original Image Name**: Ubuntu2404
- **OS**: Ubuntu 24.04
- **Admin User**: root

---

**Lưu ý**: Flow này áp dụng cho tất cả servers boot từ volume trong OpenStack. Đảm bảo bạn có đủ quyền truy cập vào Compute, Block Storage và Image services. 