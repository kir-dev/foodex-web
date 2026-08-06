/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/backend-api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
      {
        source: '/backend-userinfo',
        destination: 'http://localhost:8080/userinfo',
      },
    ];
  },
};

export default nextConfig;
