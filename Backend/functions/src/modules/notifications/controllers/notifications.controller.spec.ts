import { validate } from 'class-validator';
import { plainToInstance } from 'class-transformer';
import { NotificationQueryDto } from '../dto/notification-query.dto';

/**
 * Unit tests for NotificationQueryDto
 * Tests the query parameter parsing with @Transform decorator
 * This is the critical test for the read=false bug fix
 */
describe('NotificationQueryDto - Query Parameter Parsing', () => {
  describe('read parameter transformation', () => {
    it('should parse string "true" to boolean true', async () => {
      const plainObject = { read: 'true' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(true);
      expect(typeof dto.read).toBe('boolean');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should parse string "false" to boolean false (MAIN BUG FIX)', async () => {
      const plainObject = { read: 'false' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      // THIS IS THE CRITICAL ASSERTION FOR THE BUG FIX
      // Before the fix, this would be true (implicit conversion treated "false" as truthy)
      // After disabling enableImplicitConversion, @Transform correctly converts it to false
      expect(dto.read).toBe(false);
      expect(typeof dto.read).toBe('boolean');
      expect(dto.read === false).toBe(true);

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should parse numeric 1 to boolean true', async () => {
      const plainObject = { read: 1 };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(true);
      expect(typeof dto.read).toBe('boolean');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should parse numeric 0 to boolean false', async () => {
      const plainObject = { read: 0 };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(false);
      expect(typeof dto.read).toBe('boolean');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should parse string "1" to boolean true', async () => {
      const plainObject = { read: '1' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(true);
      expect(typeof dto.read).toBe('boolean');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should parse string "0" to boolean false', async () => {
      const plainObject = { read: '0' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(false);
      expect(typeof dto.read).toBe('boolean');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should omit read and keep as undefined when not provided', async () => {
      const plainObject = {};
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBeUndefined();

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should handle invalid read value and convert to undefined', async () => {
      const plainObject = { read: 'invalid' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      // Invalid values should return undefined
      expect(dto.read).toBeUndefined();

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should handle null read and convert to undefined', async () => {
      const plainObject = { read: null };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBeUndefined();

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });
  });

  describe('page and limit parameters', () => {
    it('should parse string page to number', async () => {
      const plainObject = { page: '2' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.page).toBe(2);
      expect(typeof dto.page).toBe('number');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should parse string limit to number', async () => {
      const plainObject = { limit: '50' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.limit).toBe(50);
      expect(typeof dto.limit).toBe('number');

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should handle all parameters together', async () => {
      const plainObject = { read: 'false', page: '2', limit: '10' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(false);
      expect(dto.page).toBe(2);
      expect(dto.limit).toBe(10);

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });
  });

  describe('Real-world query string scenarios', () => {
    it('should handle query string: read=true&page=1&limit=20', async () => {
      // Simulates: GET /api/notifications?read=true&page=1&limit=20
      const plainObject = { read: 'true', page: '1', limit: '20' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(true);
      expect(dto.page).toBe(1);
      expect(dto.limit).toBe(20);

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should handle query string: read=false&page=1&limit=20', async () => {
      // Simulates: GET /api/notifications?read=false&page=1&limit=20
      const plainObject = { read: 'false', page: '1', limit: '20' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(false);
      expect(dto.page).toBe(1);
      expect(dto.limit).toBe(20);

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should handle query string without read: page=2&limit=10', async () => {
      // Simulates: GET /api/notifications?page=2&limit=10
      const plainObject = { page: '2', limit: '10' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBeUndefined();
      expect(dto.page).toBe(2);
      expect(dto.limit).toBe(10);

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });

    it('should handle minimal query string: read=false', async () => {
      // Simulates: GET /api/notifications?read=false
      const plainObject = { read: 'false' };
      const dto = plainToInstance(NotificationQueryDto, plainObject);

      expect(dto.read).toBe(false);
      expect(dto.page).toBe(1); // default
      expect(dto.limit).toBe(20); // default

      const errors = await validate(dto);
      expect(errors).toHaveLength(0);
    });
  });
});
