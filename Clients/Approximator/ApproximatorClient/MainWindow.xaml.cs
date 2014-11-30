using System;
using ApproximatorClient.Sensors;
using WebSocketSharp;
using System.Threading;
using System.Windows.Media;

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
            ws.OnMessage += (sender, e) => Console.WriteLine(@"Response from WebSocket" + e.Data);
            ws.Connect();

            // We can communicate back with the server if we need it
            ws.Send("Hello From .NET Client");
            var userName = Environment.UserName;
            new MouseSensor(userName);
            new KeyboardSensor(userName);
            new FaceDetectionSensor(userName, cameraIndex: 0);
            Thread visualBellThread = new Thread(visualBell);
            visualBellThread.IsBackground = true;
            visualBellThread.Start();
        }

        public void visualBell()
        {
            while (true)
            {
                try
                {
                    this.Dispatcher.Invoke(new Action(() => this.Top = 0));
                    this.Dispatcher.Invoke(new Action(() => this.Left = System.Windows.SystemParameters.PrimaryScreenWidth - this.Width));
                    this.Dispatcher.Invoke(new Action(() => this.Topmost = true));
                    
                    for (int i = 0; i < 50; i++)
                    {
                        if (i % 2 == 0)
                        {
                            this.Dispatcher.Invoke(new Action(() => indicator.Fill = new SolidColorBrush(Colors.Black)));
                        }
                        else
                        {
                            this.Dispatcher.Invoke(new Action(() => indicator.Fill = new SolidColorBrush(Colors.White)));
                        }
                        Thread.Sleep(400);
                    }
                    this.Dispatcher.Invoke(new Action(() => this.Topmost = false));
                    Thread.Sleep(60 * 60 * 30); //wait for 30 minutes
                }
                catch (Exception ex)
                {
                }
            }
        }
    }
}
