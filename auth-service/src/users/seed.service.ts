import { Injectable, OnModuleInit, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from './entirires/user.entity';
import { UserRole } from './common/enums/role.enum';
import * as bcrypt from 'bcrypt';

@Injectable()
export class SeedService implements OnModuleInit {
  private readonly logger = new Logger(SeedService.name);

  constructor(
    @InjectRepository(User)
    private readonly usersRepository: Repository<User>,
  ) {}

  async onModuleInit() {
    await this.seedHrUser();
  }

  async seedHrUser() {
    try {
      // Check if HR user already exists
      const existingHr = await this.usersRepository.findOne({
        where: { role: UserRole.HR },
      });

      if (existingHr) {
        this.logger.log('HR user already exists, skipping seed');
        return;
      }

      // Create default HR user
      const hashedPassword = await bcrypt.hash('admin123', 12);
      const hrUser = this.usersRepository.create({
        email: 'hr@company.com',
        password: hashedPassword,
        employeeId: 'HR001',
        role: UserRole.HR,
        isActive: true,
        // departmentId is omitted since HR doesn't need a department
      });

      await this.usersRepository.save(hrUser);
      this.logger.log('HR user seeded successfully');
      this.logger.log('Email: hr@company.com');
      this.logger.log('Employee ID: HR001');
      this.logger.log('Password: admin123');
    } catch (error) {
      this.logger.error('Error seeding HR user:', error);
    }
  }
}
