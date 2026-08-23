export default function Footer() {
  return (
    <footer className='w-full bg-surface border-t-2 border-brand-fg px-4 py-4 text-center text-brand-fg'>
      <p>
        Made with ❤️ by{' '}
        <a
          href='https://kir-dev.hu'
          target='_blank'
          rel='noopener noreferrer'
          className='font-semibold hover:text-accent transition-all'
        >
          Kir-Dev
        </a>
      </p>
      <p className='mt-1'>Minden jog fenntartva. © 2026</p>
    </footer>
  );
}
