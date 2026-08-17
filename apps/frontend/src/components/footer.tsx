export default function Footer() {
  return (
    <footer className='w-full bg-white border-t-2 border-[#332C81] px-4 py-4 text-center text-[#332C81]'>
      <p>
        Made with ❤️ by{' '}
        <a
          href='https://kir-dev.hu'
          target='_blank'
          rel='noopener noreferrer'
          className='font-semibold hover:text-[#FF9860] transition-all'
        >
          Kir-Dev
        </a>
      </p>
      <p className='mt-1'>Minden jog fenntartva. © 2026</p>
    </footer>
  );
}
