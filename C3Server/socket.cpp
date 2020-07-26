﻿#include "socket.h"
#include <QDataStream>
#include <QByteArray>
#define IMAGEDIR "image" // where is image data
QHash<QString,QReadWriteLock> Socket::fileLocks;
Socket::Socket(qintptr socketID,QObject *parent):QThread(parent)
{
    socketDescriptor=socketID;
    dataInfo.dataSize=0;
    dataInfo.stringSize=0;
    dataInfo.dataReadedSize=0;
    qDebug()<<"a new socket create "<<socketID<<" "<<QThread::currentThreadId();
}

void Socket::run()
{
    socket=new QTcpSocket;
    socket->setSocketDescriptor(socketDescriptor);
    connect(socket,SIGNAL(readyRead()),this,SLOT(onReadyRead()),Qt::DirectConnection);
    connect(socket,SIGNAL(disconnected()),this,SLOT(quit()),Qt::DirectConnection);
    connect(this,SIGNAL(finished()),this,SLOT(deleteLater()));
    qDebug()<<"start socket thread "<<socketDescriptor<<" "<<QThread::currentThreadId();
    this->exec();
}

void Socket::onReadyRead()
{
    if(dataInfo.dataReadedSize==0)
    {
        qDebug()<<"read dataSize&&stringSize";
        if(socket->bytesAvailable()>=sizeof(quint64)*2)
        {
            QDataStream in(socket);
            in>>dataInfo.dataSize>>dataInfo.stringSize;
            qDebug()<<"dataSize="<<dataInfo.dataSize<<",stringSize="<<dataInfo.stringSize;
            dataInfo.dataReadedSize=2*sizeof(quint64);
            if(socket->bytesAvailable()+dataInfo.dataReadedSize>=dataInfo.dataSize)
            {
                QString filename=QString::fromUtf8(socket->read(dataInfo.stringSize),dataInfo.stringSize);
                dataInfo.dataReadedSize+=dataInfo.stringSize;
                if(dataInfo.dataReadedSize==dataInfo.dataSize)
                {
                    qDebug()<<"process Msg";
                    dataInfo.dataSize=0;dataInfo.stringSize=0;dataInfo.dataReadedSize=0;//reset dataInfo
                    processMsg(filename);
                }
                else
                {
                    qDebug()<<"Read file";
                    readFile(filename);
                }
            }
        }
    }else
    {
        if(socket->bytesAvailable()+dataInfo.dataReadedSize>=dataInfo.dataSize)
        {
            QString filename=QString::fromUtf8(socket->read(dataInfo.stringSize),dataInfo.stringSize);
            dataInfo.dataReadedSize+=dataInfo.stringSize;
            if(dataInfo.dataReadedSize==dataInfo.dataSize)
            {
                qDebug()<<"process Msg";
                dataInfo.dataSize=0;dataInfo.stringSize=0;dataInfo.dataReadedSize=0;//reset dataInfo
                processMsg(filename);
            }
            else
            {
                qDebug()<<"Read file";
                readFile(filename);
            }
        }
    }
}

void Socket::readFile(const QString &filename)
{

    QByteArray block=socket->read(dataInfo.dataSize-dataInfo.dataReadedSize);
    QRegExp blockSetRex("blockSet__(.*)__(.*)__(.*)__(.*)__(.*)__(.*)__(.*).swc");
    QString filePath;
    if(blockSetRex.indexIn(filename)!=-1)
        filePath=QCoreApplication::applicationDirPath()+"/tmp/"+filename;
    else
        filePath=QCoreApplication::applicationDirPath()+"/data/"+filename;
    QFile file(filePath);
    file.open(QIODevice::WriteOnly);
    file.write(block);
    file.close();
    dataInfo.dataSize=0;dataInfo.stringSize=0;dataInfo.dataReadedSize=0;//reset dataInfo

    qDebug()<<"Read file end "<<filename;
    if(blockSetRex.indexIn(filename)!=-1)
    {
        QString name=blockSetRex.cap(1)+".swc";
        int x1=blockSetRex.cap(2).toInt();
        int x2=blockSetRex.cap(3).toInt();
        int y1=blockSetRex.cap(4).toInt();
        int y2=blockSetRex.cap(5).toInt();
        int z1=blockSetRex.cap(6).toInt();
        int z2=blockSetRex.cap(7).toInt();
        setSwcInBB(name,x1,x2,y1,y2,z1,z2);
        qDebug()<<"set SWC END";
    }
    //should move set to server to keep only one to change
}

void Socket::processMsg(const QString &msg)
{
    QRegExp LoginRex("(.*):login.\n");

    QRegExp DownRex("(.*):down.\n");
    QRegExp FileDownRex("(.*):choose1.\n");

    QRegExp ImageDownRex("(.*):choose3.\n");//要求发送全脑图像列表
    QRegExp BrainNumberRex("(.*):BrainNumber.\n");//脑图像编号
    QRegExp ImgBlockRex("(.*):imgblock.\n");

    QRegExp GetBBSWCRex("(.*):GetBBSwc.\n");

    qDebug()<<"MSG:"<<msg;
    if(LoginRex.indexIn(msg)!=-1)
    {
        QString username=LoginRex.cap(1).trimmed();
        sendMsg(QString(username+":log in success."));
    }else if(DownRex.indexIn(msg)!=-1)
    {
        sendMsg(QString(currentDir()+":currentDir_down"));
    }else if(FileDownRex.indexIn(msg)!=-1)
    {
        QString filename=FileDownRex.cap(1).trimmed();
        sendFile(filename,0);//0:data/filename //
    }else if(ImageDownRex.indexIn(msg)!=-1)
    {
        sendMsg(currentImg()+":currentDirImg");
    }else if(BrainNumberRex.indexIn(msg)!=-1)
    {
        QString filename=BrainNumberRex.cap(1).trimmed();
        sendFile(filename+".txt",1);//image/brainnumber/filename.txt
    }
    else if(ImgBlockRex.indexIn(msg)!=-1)
    {
        getAndSendImageBlock(ImgBlockRex.cap(1).trimmed());
    }else if(GetBBSWCRex.indexIn(msg)!=-1)
    {
        getAndSendSWCBlock(GetBBSWCRex.cap(1).trimmed());//mul read and write
    }
    qDebug()<<"process Msg end";
}
//data file :mul read and write
//image file:mul read
//brainInfo file:mul read
void Socket::sendMsg(const QString &msg) const
{
    QByteArray block;
    QDataStream dts(&block,QIODevice::WriteOnly);
    dts<<quint64(0)<<quint64(0)<<msg.toUtf8();
    dts.device()->seek(0);
    dts<<(quint64)(block.size())<<(quint64)(block.size()-sizeof(quint64)*2)<<msg.toUtf8();
    socket->write(block);
    socket->waitForBytesWritten();
}
void Socket::sendFile(const QString &filename, int type) const
{
    //type:use it to find Dir;
    //need to modify
    //type:0 down file from data dir
    //type:1 down brain file from neuronInfo dir;
    //type:2 send image block/bb block from tmp dir;
    QString filePath;
    switch (type) {
        case 0:filePath.clear();filePath=QCoreApplication::applicationDirPath()+"/data/"+filename;break;
        case 1:filePath.clear();filePath=QCoreApplication::applicationDirPath()+"/brainInfo/"+filename;break;
        case 2:filePath.clear();filePath=QCoreApplication::applicationDirPath()+"/tmp/"+filename;break;
    default: break;
    }
    qDebug()<<"filepath:"<<filePath;
    QFile f(filePath);
    if(f.exists()&&socket->state()==QAbstractSocket::ConnectedState)
    {
        __START:
        if(f.open(QIODevice::ReadOnly))
        {

            QByteArray filedata=f.readAll();
            QByteArray block;
            QDataStream dts(&block,QIODevice::WriteOnly);
            dts<<quint64(0)<<quint64(0)<<filename.toUtf8()<<filedata;
            dts.device()->seek(0);
            dts<<(quint64)(block.size())
              <<(quint64)(block.size()-sizeof(quint64)*2-filedata.size())<<filename.toUtf8()<<filedata;

            socket->write(block);
            socket->waitForBytesWritten();
            qDebug()<<"send "<<filePath<<" success ";

            f.close();

            if(type==2) f.remove();
        }else
        {
            QElapsedTimer t;
            t.start();
            while(t.elapsed()<2000);
            goto __START;
        }
    }
    else
    {
        qDebug()<<"can not send "<<filePath<<" please check it!";
    }
}
QString Socket::currentDir() const
{
    QString dataPath=QCoreApplication::applicationDirPath()+"/data";
    QStringList dataFileList=QDir(dataPath).entryList(QDir::Files|QDir::NoDotAndDotDot);
    return  dataFileList.join(";");
}

QString Socket::currentImg() const
{
    QString imgPath=QCoreApplication::applicationDirPath()+"/"+IMAGEDIR;
    QStringList imgDirList=QDir(imgPath).entryList(QDir::Dirs|QDir::NoDotAndDotDot);
    return imgDirList.join(";");
}

void Socket::getAndSendSWCBlock(QString msg)
{
    QRegExp tmp("(.*)__(.*)__(.*)__(.*)__(.*)__(.*)__(.*)");
    int n=5;//重复5次，每次延时2S
    if(tmp.indexIn(msg)!=-1)
    {
        QString name=tmp.cap(1)+".swc";
        int x1=tmp.cap(2).toInt();
        int x2=tmp.cap(3).toInt();
        int y1=tmp.cap(4).toInt();
        int y2=tmp.cap(5).toInt();
        int z1=tmp.cap(6).toInt();
        int z2=tmp.cap(7).toInt();

        __START:
        NeuronTree nt;
        --n;
        qDebug()<<"Get SWC in BB:"<<5-n;
        nt=readSWC_file(QCoreApplication::applicationDirPath()+"/data/"+name);
        if(nt.flag!=false)
        {
            V_NeuronSWC_list testVNL=NeuronTree__2__V_NeuronSWC_list(nt);
            V_NeuronSWC_list tosave;
            for(int i=0;i<testVNL.seg.size();i++)
            {
                NeuronTree SS;
                V_NeuronSWC seg_temp =  testVNL.seg.at(i);
                seg_temp.reverse();
                for(int j=0;j<seg_temp.row.size();j++)
                {
                    if(seg_temp.row.at(j).x>=x1&&seg_temp.row.at(j).x<=x2
                            &&seg_temp.row.at(j).y>=y1&&seg_temp.row.at(j).y<=y2
                            &&seg_temp.row.at(j).z>=z1&&seg_temp.row.at(j).z<=z2)
                    {
                        tosave.seg.push_back(seg_temp);
                        break;
                    }
                }
            }
            nt=V_NeuronSWC_list__2__NeuronTree(tosave);
            for(int i=0;i<nt.listNeuron.size();i++)
            {
                nt.listNeuron[i].x-=x1;
                nt.listNeuron[i].y-=y1;
                nt.listNeuron[i].z-=z1;
            }
            if(!QDir(QCoreApplication::applicationDirPath()+"/tmp").exists())
            {
                QDir(QCoreApplication::applicationDirPath()).mkdir("tmp");
            }
            QString BBSWCNAME=QCoreApplication::applicationDirPath()+"/tmp/blockGet__"+QFileInfo(name).baseName()+QString("__%1__%2__%3__%4__%5__%6.swc")
                    .arg(x1).arg(x2).arg(y1).arg(y2).arg(z1).arg(z2);
            writeSWC_file(BBSWCNAME,nt);
            sendFile("blockGet__"+QFileInfo(name).baseName()+QString("__%1__%2__%3__%4__%5__%6.swc")
                     .arg(x1).arg(x2).arg(y1).arg(y2).arg(z1).arg(z2),2);
            return;
        }
        else
        {
            if(!n)
            {
                qDebug()<<"error:"<<msg<<" failed 5 times to get SWC IN BB:"<<msg;
                goto __ERROR;
            }else
            {

                QElapsedTimer t;
                t.start();
                while(t.elapsed()<2000);
                goto __START;
            }
        }
    }else
    {
        qDebug()<<"error:"<<msg<<" does not match tmp";
    }
    __ERROR:
        sendMsg(QString("Can't get the SWC in BB ,please try again??%1:ERROR").arg(msg+":GetBBSwc.\n"));
}

void Socket::getAndSendImageBlock(QString msg)
{
    qDebug()<<"getAndSendImageBlock:"<<msg;
    QStringList paraList=msg.split("__",QString::SkipEmptyParts);
    QString filename=paraList.at(0).trimmed();//1. tf name/RES  2. .v3draw// test:17302/RES54600x34412x9847__x__y__z_b;
    QString filename1=filename;
    filename1=filename1.remove('/');
    qDebug()<<"filename:"<<filename;
    qDebug()<<"filename1:"<<filename1;
//0: 18465/RESx18000x13000x5150
//1: 12520
//2: 7000
//3: 2916
    int xpos=paraList.at(1).toInt();
    int ypos=paraList.at(2).toInt();
    int zpos=paraList.at(3).toInt();
    int blocksize=paraList.at(4).toInt();
    if(!QDir(QCoreApplication::applicationDirPath()+"/tmp").exists())
    {
        QDir(QCoreApplication::applicationDirPath()).mkdir("tmp");
    }
    QString string=QCoreApplication::applicationDirPath()+"/tmp/"+QString::number(socket->socketDescriptor())+filename1+"__"
                  + QString::number(xpos)+ "__"
                  + QString::number(ypos)+ "__"
                  + QString::number(zpos)+ "__"
                  + QString::number(blocksize)+"__"
                  + QString::number(blocksize)+ "__"
                  + QString::number(blocksize);

    qDebug()<<string;
    QProcess p;

    CellAPO centerAPO;
    centerAPO.x=xpos;centerAPO.y=ypos;centerAPO.z=zpos;
    QList <CellAPO> List_APO_Write;
    List_APO_Write.push_back(centerAPO);
    if(!writeAPO_file(string+".apo",List_APO_Write))
    {
        qDebug()<<"fail to write apo";
        return;//get .apo to get .v3draw
    }

    QString namepart1=QString::number(socket->socketDescriptor())+"_"+filename1+QString::number(blocksize)+"_";
    QString vaa3dPath=QCoreApplication::applicationDirPath();
    QString order =QString("xvfb-run -a %0/vaa3d -x %1/plugins/image_geometry/crop3d_image_series/libcropped3DImageSeries.so "
                            "-f cropTerafly -i %2/%3/ %4.apo %5/tmp/%6 -p %7 %8 %9")
            .arg(vaa3dPath).arg(vaa3dPath)
            .arg(QCoreApplication::applicationDirPath()+"/"+IMAGEDIR).arg(filename).arg(string).arg(QCoreApplication::applicationDirPath()).arg(namepart1).arg(blocksize).arg(blocksize).arg(blocksize);
    qDebug()<<"order="<<order;
    if(p.execute(order.toStdString().c_str())!=-1||p.execute(order.toStdString().c_str())!=-2)
    {
        QFile f1(string+".apo"); qDebug()<<f1.remove();
        QString fName=namepart1+QString("%1.000_%2.000_%3.000.v3dpbd").arg(xpos).arg(ypos).arg(zpos);
        qDebug()<<fName<<"*************";
        sendFile(fName,2);
    }else
    {
        sendMsg(QString("Can't get the image in BB ,please try again??%1:ERROR").arg(msg+":imgblock.\n"));
    }
}

void Socket::setSwcInBB(QString name, int x1, int x2, int y1, int y2, int z1, int z2)
{
    V_NeuronSWC_list testVNL;
    V_NeuronSWC_list resVNL;
    resVNL=testVNL;
    resVNL.seg.clear();
    int n=5;
    __START:
    qDebug()<<"try "<<5-n+1;
    if(QFile(QCoreApplication::applicationDirPath()+"/data/"+name).exists())
    {
        --n;
        NeuronTree nt=readSWC_file(QCoreApplication::applicationDirPath()+"/data/"+name);
        if(nt.flag==false)
        {
            if(n)
            {
                QElapsedTimer t;
                t.start();
                while(t.elapsed()<2000);
                goto __START;
            }else
            {
                qDebug()<<"FATAL:can not set SWC "<<name<<" call coder to check";
                return;
            }
        }
        testVNL=NeuronTree__2__V_NeuronSWC_list(nt);
        for(int i=0;i<testVNL.seg.size();i++)
        {
            testVNL.seg[i].to_be_deleted=0;
            V_NeuronSWC seg_temp =  testVNL.seg.at(i);
            seg_temp.reverse();
            for(int j=0;j<seg_temp.row.size();j++)
            {
                if(seg_temp.row.at(j).x>=x1&&seg_temp.row.at(j).x<=x2
                        &&seg_temp.row.at(j).y>=y1&&seg_temp.row.at(j).y<=y2
                        &&seg_temp.row.at(j).z>=z1&&seg_temp.row.at(j).z<=z2)
                {
                    testVNL.seg[i].to_be_deleted=1;
                    break;
                }
            }
        }
        for(int i=0;i<testVNL.seg.size();i++)
        {
            if(testVNL.seg[i].to_be_deleted==0)
                resVNL.seg.push_back(testVNL.seg.at(i));
        }
    }
    qDebug()<<"open blockSet file";
    QString BBSWCNAME="blockSet__"+QFileInfo(name).baseName()+QString("__%1__%2__%3__%4__%5__%6.swc")
            .arg(x1).arg(x2).arg(y1).arg(y2).arg(z1).arg(z2);
    NeuronTree nt=readSWC_file(QCoreApplication::applicationDirPath()+"/tmp/"+BBSWCNAME);
    for(int i=0;i<nt.listNeuron.size();i++)
    {
        nt.listNeuron[i].x+=x1;
        nt.listNeuron[i].y+=y1;
        nt.listNeuron[i].z+=z1;
    }
    V_NeuronSWC_list testVNL1=NeuronTree__2__V_NeuronSWC_list(nt);
    for(int i=0;i<testVNL1.seg.size();i++)
    {
        resVNL.seg.push_back(testVNL1.seg.at(i));
    }
    nt=V_NeuronSWC_list__2__NeuronTree(resVNL);
    while(!writeESWC_file(QCoreApplication::applicationDirPath()+"/data/"+name,nt))
    {
        QElapsedTimer t;
        t.start();
        while(t.elapsed()<2000);
    }
    QFile f(QCoreApplication::applicationDirPath()+"/tmp/"+BBSWCNAME);
    f.remove();
}
