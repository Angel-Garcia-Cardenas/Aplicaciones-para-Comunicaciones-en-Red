%Entregable 2
%Señal diezmada
%Equipo 6

%García Cárdenas Ángel Alberto
%Lugo Navarro Alan Michel
%Orta Acuña Angel Gabriel
%Perrusquia Islas Milton Carlos
%Rodríguez García Pedro Uriel

fs = 400;
t = [0:1/fs:1.5];
y = 2 * sin (4 * pi * t);
%diezmado
ydecimated = decimate (y, 4);

%Señal original
subplot(2, 2, 1);
stem (0:199, y(1:200), "MarkerSize",3);
grid on
xlabel ("Tiempo");
ylabel ("y(n) - Original");
%Señal diezmada
subplot (2, 2, 2);
stem (0:49, ydecimated(1:50), "MarkerSize",3);
grid on
xlabel ("Tiempo");
ylabel("y(n) - Diezmada");

%Interpolación
yinterpolated = interp (y, 4);
%Señal original
subplot (2, 2, 3);
stem (0:49, y(1:50), "MarkerSize",3);
grid on
xlabel ("Tiempo");
ylabel("y(n) - Original");
%Señal interpolada
subplot(2, 2, 4);
stem (0:199, yinterpolated(1:200), "MarkerSize",3);
grid on
xlabel ("Tiempo");
ylabel("y(n) - Interpolada");
