/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        // Minden, ami a frontendről /backend-api/-val indul...
        source: '/backend-api/:path*',
        // ...az valójában a háttérben a localhost:8080/api/ címre irányítódik át!
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};

export default nextConfig;
