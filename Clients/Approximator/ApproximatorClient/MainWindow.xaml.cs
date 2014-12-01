using System;
using ApproximatorClient.Sensors;
using WebSocketSharp;
using System.IO;
using System.Threading;
using System.Windows.Media;
using System.Media;
using System.Windows;
using log4net;
using log4net.Config;

namespace ApproximatorClient
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow
    {
        private static readonly ILog promptLog = LogManager.GetLogger("PromptLogger");
        private static readonly ILog dataLog = LogManager.GetLogger("DataLogger");
        public MainWindow()
        {
            InitializeComponent();
            var ws = new WebSocket("ws://spcl.cloudapp.net:6696/users");
            ws.OnMessage += (sender, e) => dataLog.Info(@"Response from WebSocket" + e.Data);
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
            var sound = new SoundPlayer(@"Resources\Notify.wav");
            while (true)
            {
                try
                {
                    
                    this.Dispatcher.Invoke(new Action(() => this.Top = 0));
                    this.Dispatcher.Invoke(new Action(() => this.Left = System.Windows.SystemParameters.PrimaryScreenWidth - this.Width));
                    this.Dispatcher.Invoke(new Action(() => this.Topmost = true));
                    
                    for (int i = 0; i <= 20; i++)
                    {
                        if (i % 2 == 0)
                        {
                            this.Dispatcher.Invoke(new Action(() => indicator.Fill = new SolidColorBrush(Colors.Black)));
                        }
                        else
                        {
                            this.Dispatcher.Invoke(new Action(() => indicator.Fill = new SolidColorBrush(Colors.White)));
                        }
                        if (i % 10 == 0)
                        {
                            sound.Play();
                        }
                        Thread.Sleep(200);
                    }
                    this.Dispatcher.Invoke(new Action(() => this.Topmost = false));
                    promptLog.Info("Prompted user");
                    Thread.Sleep(new Random(1000*60*60*20).Next(1000*60*60*30)); //wait between 20-30 minutes
                }
                catch (Exception ex)
                {
                }
            }
        }
    }
}
