using System;
using ApproximatorClient.Sensors;
using WebSocketSharp;

namespace ApproximatorClient
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow
    {
        public MainWindow()
        {
            InitializeComponent();
            var ws = new WebSocket("ws://spcl.cloudapp.net:6696/users");
            ws.OnMessage += (sender, e) => Console.WriteLine("Response from WebSocket" + e.Data);
            ws.Connect();

            // We can communicate back with the server if we need it
            ws.Send("Hello From .NET Client");

            new MouseSensor("User1");
            new KeyboardSensor("User1");
        }
    }
}
