# RPoLMonitor

RPoLMonitor is a small android app that connects to the RPoL website and warns you when there are new messages waiting for you.

## Installation

The application is not yet available on the Play Store, which means:
- You need to download it from here: https://drive.google.com/open?id=10uKPE3KC8xZgRKjiuvXrKAO8kxT-xzSf
- Your device will probably warn you about potential security issues (you'll have to tell it that yes, it's ok to install this, really).
- It should run on most relatively recent devices, but I can't promise much of anything.

## Usage

Just enter your login / password. Every X minute (interval that you can set between 1 and 60 minutes), it will look at the RPoL homepage and look for anything new. If it detects anything, it will send a notification. Taping on it will bring you to the main page of the application from which you can access the games via your browser.

## Security concerns

The application will obviously ask for your login / password to do its job. I've put the code of the whole project so that anyone who can and wants is able to check that I do not send that information anywhere but on the RPoL server (via HTTPS connection).
Another thing concerns saving your password. I gave that possibility, but you have to be aware that the password is saved somewhere on your device, and it is not encrypted in anyway. It's not that I don't want to, but that I can't (or I could, but I'd need to also save the decryption process, which is exactly the same in the end).

## Contributing

It's pretty simple, and it's pretty basic. I had never developped mobile applications before, and I didn't even spend two days on this. That means there are probably lots of things to add / improve. Some I may do, some I simply can't (for example, I'm not a designer, don't ask me for pretty icons, images or whatever, I can't).

If you want to help with that (or add features or whatever), you can contact me at imladir@posteo.org or on the new RPoL Discord Channel (nick Imladir).
