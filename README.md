# Expensention

Expensention is an Android application designed to streamline expense and income management. What sets Expensention apart from other apps is its ability to first input the transaction amount and, when UPI is selected, scan the QR code before automatically redirecting the user to their preferred UPI app. This eliminates the need to separately log expenses and handle payments across different apps.

## Screenshots

<div style="display: flex;">
    <img src="https://github.com/user-attachments/assets/6a8a8205-d46c-41a7-b529-9f855b5fa3ad" width="24%" style="margin-right: 10px;">
    <img src="https://github.com/user-attachments/assets/b8e2d655-a78f-4c7a-a0de-75c3a8f83f99" width="24%" style="margin-right: 10px;">
    <img src="https://github.com/user-attachments/assets/af903c2e-d046-4cfa-8150-ad3b9797bc16" width="24%" style="margin-right: 10px;">
    <img src="https://github.com/user-attachments/assets/85f0bf12-bbe6-4a73-930c-4484faba3724" width="24%">
</div>

## Key Features

- **Unified Transaction Management**: Manage all your transactions—expenses or income—within one app.
- **Seamless UPI Payments**: Input the amount first, scan the UPI QR code, and get redirected to the appropriate app for payment, ensuring you don’t miss logging any transactions.
- **Efficient QR Scanning**: Quickly scan UPI QR codes using the Quickie QR library.
- **Centralized Transaction Log**: All your transactions, including UPI payments across different apps, are stored in one place for easy reference.
- **Local Data Storage**: Transactions are securely stored using Room database.

## Why Expensention?

Unlike other apps that require you to note down expenses separately after making payments in different apps, Expensention integrates both processes. It ensures that every transaction is logged when you make a UPI payment—saving time and keeping your finances organized.

## Installation

### Option 1: Install via APK

1. Download the APK from the [App-Release page](https://github.com/bhavyammodi/Expensention/releases).
2. Install the APK on your Android device.

### Option 2: Build from Source

1. Clone the repository:
    ```sh
    git clone https://github.com/bhavyammodi/Expensention.git
    ```
2. Open the project in Android Studio.
3. Build the project to install dependencies.
4. Connect your device or start an emulator.
5. Click "Run" in Android Studio to install and launch the app.

## Usage

1. **Add Transaction**: Click on "Add Transaction," enter the amount, choose the transaction type (expense or income), and select UPI if applicable.
2. **Scan UPI QR Code**: After inputting the amount, scan the UPI QR code. The app will redirect you to your chosen UPI app to complete the payment.
3. **Track Transactions**: Every transaction, including UPI payments, will be logged and displayed on the main screen for easy tracking.

## Future Features

1. **Time of the Transactions**: Track and display the exact time when each transaction was made.
2. **Payment Medium**: Identify the payment method used — UPI app, cash, or card.
3. **Export/Import to CSV**: Provide an option to export transaction data to CSV format and import it back for seamless backup and data migration.

## Dependencies

- [AndroidX Core](https://developer.android.com/jetpack/androidx/releases/core)
- [Material Components](https://material.io/develop/android)
- [Quickie QR](https://github.com/G00fY2/quickie)
- [Room Database](https://developer.android.com/jetpack/androidx/releases/room)

## Acknowledgments

- [Quickie QR Library](https://github.com/G00fY2/quickie)
