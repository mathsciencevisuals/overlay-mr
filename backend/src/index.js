import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import {router} from './routes/index.js';

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json({limit: '10mb'}));
app.use('/api', router);

app.get('/health', (_req, res) => {
  res.json({ok: true, service: 'overlay-mr-backend', date: new Date().toISOString()});
});

const port = Number(process.env.PORT || 8080);
app.listen(port, () => {
  console.log(`overlay backend listening on ${port}`);
});
