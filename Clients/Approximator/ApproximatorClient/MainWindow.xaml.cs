using System;
using System.Collections.Generic;
using System.IO;
using System.Media;
using System.Threading;
using System.Windows;
using System.Windows.Media;
using ApproximatorClient.Sensors;
using log4net;
using WebSocketSharp;

namespace ApproximatorClient
{
    public partial class MainWindow
    {
        private static readonly ILog PromptLog = LogManager.GetLogger("PromptLogger");
        private static readonly ILog DataLog = LogManager.GetLogger("DataLogger");
        private WebSocket _ws;
        private string _userName;

        public MainWindow()
        {
            //InitializeComponent();

            TestAlgorithm();

            //            ConnectWebSocket();
//            StartSensing();
//            StartPrompting();
            Application.Current.Shutdown();
        }

        private static void TestAlgorithm()
        {
            // Setup logging file
            const string filePath = @"AlgorithmTest.txt";
            if (File.Exists(filePath)) File.Delete(filePath);
            if (!File.Exists(filePath))
            {
                var createText = "Algorithm Test" + Environment.NewLine;
                File.WriteAllText(filePath, createText);
                var appendText = "==============" + Environment.NewLine + Environment.NewLine;
                File.AppendAllText(filePath, appendText);
            }

            // These are the ground truth values reported during the test
            var andersExpected = new List<int> {3, 4, 1, 5, 4, 1, 3, 1, 4, 5, 2, 5, 2};
            var madsExpected = new List<int> {1, 2, 5, 4, 2, 4, 5, 2, 1, 4, 5, 1};

            var andersDifference = 0;

            // Here we iterate through them against the server checking the server algorithm
            var index = 1;
            foreach (var expected in andersExpected)
            {
                var path = @"C:\Users\Anders\Desktop\Git\GSE\Data from test\Videos and data\Anders\ABM-" + index + "-Data.txt";
                var sensorReading = Replayer.ReplayPath(path);
                var result = ServerHandler.GetInterruptibilityAndClearReadings(sensorReading);
                var interruptibility = Int32.Parse(result);
                var difference = Math.Abs(expected - interruptibility);
                andersDifference += difference;
                var resultText =
                    String.Format(
                    "Anders prompt {0}. Interruptibility was {1}, we expected it to be {2}. The difference is {3}.",
                        index, interruptibility, expected, difference);
                File.AppendAllText(filePath, resultText + Environment.NewLine);
                index++;
            }

            var andersResults = String.Format("Out of {0} readings for Anders, there was a difference of {1}", andersExpected.Count, andersDifference);
            File.AppendAllText(filePath, andersResults + Environment.NewLine + Environment.NewLine);

            var madsDifference = 0;
            index = 1;
            foreach (var expected in madsExpected)
            {
                var path = @"C:\Users\Anders\Desktop\Git\GSE\Data from test\Videos and data\Mads\day 1\prompt" + index + ".txt";
                var sensorReading = Replayer.ReplayPath(path);
                var result = ServerHandler.GetInterruptibilityAndClearReadings(sensorReading);
                var interruptibility = Int32.Parse(result);
                var difference = Math.Abs(expected - interruptibility);
                madsDifference += difference;
                var resultText =
                    String.Format(
                    "Mads prompt {0}. Interruptibility was {1}, we expected it to be {2}. The difference is {3}.",
                        index, interruptibility, expected, difference);
                File.AppendAllText(filePath, resultText + Environment.NewLine);
                index++;
            }

            var madsResults = String.Format("Out of {0} readings for Mads, there was a difference of {1}", madsExpected.Count, madsDifference);
            File.AppendAllText(filePath, madsResults + Environment.NewLine + Environment.NewLine);

            var results = String.Format("Out of {0} total readings, there was a total difference of {1}", andersExpected.Count + madsExpected.Count, andersDifference + madsDifference);
            File.AppendAllText(filePath, results + Environment.NewLine);
        }

        private void StartSensing()
        {
            new MouseSensor(_userName, _ws);
            new KeyboardSensor(_userName, _ws);
            new FaceDetectionSensor(_userName, _ws, cameraIndex: 0);
        }

        private void StartPrompting()
        {
            var visualBellThread = new Thread(VisualBell) {IsBackground = true};
            visualBellThread.Start();
        }

        private void ConnectWebSocket()
        {
//            _ws = new WebSocket("ws://spcl.cloudapp.net:6696/users");
            _ws = new WebSocket("ws://localhost:6696/users");
            _ws.OnMessage += (sender, e) => DataLog.Info(@"Response from WebSocket" + e.Data);
            _ws.Connect();
            _userName = Environment.UserName.Replace(" ", "");
            _ws.Send("Connected .NET Client for user " + _userName);
        }

        public void VisualBell()
        {
            var sound = new SoundPlayer(@"Resources\Notify.wav");
            var random = new Random();
            while (true)
            {
                Dispatcher.Invoke(new Action(() => Top = 0));
                Dispatcher.Invoke(new Action(() => Left = SystemParameters.PrimaryScreenWidth - Width));
                Dispatcher.Invoke(new Action(() => Topmost = true));
                for (var i = 0; i <= 20; i++)
                {
                    Dispatcher.Invoke(i%2 == 0
                        ? (() => indicator.Fill = new SolidColorBrush(Colors.Black))
                        : new Action(() => indicator.Fill = new SolidColorBrush(Colors.White)));
                    if (i%10 == 0)
                        sound.Play();
                    Thread.Sleep(200);
                }
                Dispatcher.Invoke(new Action(() => Topmost = false));
                PromptLog.Info("Prompted user: " + _userName);
                _ws.Send("Prompted user: " + _userName);
                const int minutes20 = 60000*20;
                const int minutes40 = 60000*40;
                Thread.Sleep(random.Next(minutes20, minutes40)); // sleep for a random interval between 20-40 minutes
            }
        }
    }
}
