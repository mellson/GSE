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
            var ws = new WebSocket("ws://localhost:6696/connect");
            ws.OnMessage += (sender, e) => Console.WriteLine(e.Data);
            ws.Connect();                
            ws.Send("BALUS");


                //            new MouseSensor("User1");
                //            new KeyboardSensor("User1");
//                Console.ReadKey(true);
            
        }
    }
}
