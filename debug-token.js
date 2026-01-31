// 调试Token的脚本
// 在浏览器控制台运行

const token = localStorage.getItem('token');
console.log('Token:', token);

if (token) {
    const parts = token.split('.');
    console.log('Parts count:', parts.length);
    
    if (parts.length === 3) {
        try {
            const header = JSON.parse(atob(parts[0]));
            const payload = JSON.parse(atob(parts[1]));
            
            console.log('Header:', header);
            console.log('Payload:', payload);
            console.log('签发时间:', new Date(payload.iat * 1000));
            console.log('过期时间:', new Date(payload.exp * 1000));
            console.log('当前时间:', new Date());
            console.log('是否过期:', payload.exp * 1000 < Date.now());
            
            if (payload.exp * 1000 < Date.now()) {
                console.log('Token已过期，建议清除');
                // localStorage.removeItem('token');
                // localStorage.removeItem('userInfo');
                // location.reload();
            } else {
                console.log('Token仍然有效');
            }
        } catch (e) {
            console.error('解析Token失败:', e);
        }
    }
}
