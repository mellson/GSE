using System;
using System.Threading;
using Approximator.Sensors;

namespace Approximator
{
    class Program
    {

        static void Main()
        {
            Console.Out.WriteLine("Approximator Rocks");

            var mouseSensor = new MouseSensor();
            while (true)
            {
                Thread.Sleep(300);
            }
            Console.ReadKey();
        }
    }
}
